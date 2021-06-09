package ru.nya.push.service.fcm.xmpp;

import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.springframework.context.ApplicationEventPublisher;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import ru.nya.push.service.fcm.FcmSettings;
import ru.nya.push.service.fcm.xmpp.backoff.BackOffStrategy;
import ru.nya.push.service.fcm.xmpp.backoff.RetryFailedException;
import ru.nya.push.service.fcm.xmpp.events.ConnectionIsReady;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class CcsClient implements ConnectionListener, PingFailedListener {

    // For the FCM connection
    public static final String FCM_SERVER = "fcm-xmpp.googleapis.com"; // prod
    public static final int FCM_PORT = 5236; // prod
    public static final String FCM_ELEMENT_NAME = "gcm";
    public static final String FCM_NAMESPACE = "google:mobile:data";
    public static final String FCM_SERVER_AUTH_CONNECTION = "gcm.googleapis.com";

    private XMPPTCPConnection xmppConnection;
    private boolean isConnectionDraining = false;

    private final MessageReceiver messageReceiver;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String apiKey;
    private final String username;

    public CcsClient(FcmSettings settings, MessageReceiver messageReceiver, ApplicationEventPublisher applicationEventPublisher) {
        this.messageReceiver = messageReceiver;
        this.applicationEventPublisher = applicationEventPublisher;
        this.apiKey = settings.getServerKey();
        this.username = settings.getProjectSenderId() + "@" + FCM_SERVER_AUTH_CONNECTION;
        // Add FCM Packet Extension Provider
        ProviderManager.addExtensionProvider(FcmPacketExtension.FCM_ELEMENT_NAME, FcmPacketExtension.FCM_NAMESPACE,
                new ExtensionElementProvider<FcmPacketExtension>() {
                    @Override
                    public FcmPacketExtension parse(XmlPullParser parser, int initialDepth)
                            throws XmlPullParserException, IOException {
                        final String json = parser.nextText();
                        return new FcmPacketExtension(json);
                    }
                });
        try {
            connect();
        } catch (XMPPException | InterruptedException | KeyManagementException | NoSuchAlgorithmException
                | SmackException | IOException e) {
            log.error("Error trying to connect. Error: {}", e.getMessage());
        }
    }

    /**
     * Connects to FCM Cloud Connection Server using the supplied credentials
     */
    public void connect() throws XMPPException, SmackException, IOException, InterruptedException,
            NoSuchAlgorithmException, KeyManagementException {
        log.info("Initiating connection ...");

        // create connection configuration
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, new SecureRandom());
        SmackConfiguration.DEBUG = false;

        final XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("FCM XMPP Client Connection Server")
                .setHost(FCM_SERVER)
                .setPort(FCM_PORT)
                .setSendPresence(false)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                .setCompressionEnabled(true)
                .setSocketFactory(sslContext.getSocketFactory())
                .setCustomSSLContext(sslContext)
                .build();

        xmppConnection = new XMPPTCPConnection(config); // Create the connection
        log.info("Connecting to the server ...");
        xmppConnection.connect(); // Connect

        // Enable automatic reconnection
        ReconnectionManager.getInstanceFor(xmppConnection).enableAutomaticReconnection();

        // Disable Roster at login (in XMPP the contact list is called a "roster")
        Roster.getInstanceFor(xmppConnection).setRosterLoadedAtLogin(false);

        // Security checks
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN"); // FCM CCS requires a SASL PLAIN authentication mechanism
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        log.info("SASL PLAIN authentication enabled ? {}", SASLAuthentication.isSaslMechanismRegistered("PLAIN"));
        log.info("Is compression enabled ? {}", xmppConnection.isUsingCompression());
        log.info("Is the connection secure ? {}", xmppConnection.isSecureConnection());

        // Handle connection errors
        xmppConnection.addConnectionListener(this);

        // Handle incoming packets and reject messages that are not from FCM CCS
        xmppConnection.addAsyncStanzaListener(messageReceiver, stanza -> stanza.hasExtension(FCM_ELEMENT_NAME, FCM_NAMESPACE));

        // Log all outgoing packets
        xmppConnection.addStanzaInterceptor(stanza -> log.info("Sent: {}", stanza.toXML(null)), ForEveryStanza.INSTANCE);

        // Set the ping interval
        final PingManager pingManager = PingManager.getInstanceFor(xmppConnection);
        pingManager.setPingInterval(1000);
        pingManager.registerPingFailedListener(this);

        xmppConnection.login(username, apiKey);
        log.info("User logged in: {}", username);
    }

    @Override
    public void pingFailed() {
        log.info("The ping failed, restarting the ping interval again ...");
        final PingManager pingManager = PingManager.getInstanceFor(xmppConnection);
        pingManager.setPingInterval(100);
    }

    @Override
    public void connected(XMPPConnection connection) {
        log.info("Connection established.");
    }

    // This is the last step after a connection or reconnection
    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        log.info("User authenticated.");
        applicationEventPublisher.publishEvent(new ConnectionIsReady(this));
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        log.info("Connection closed on error.");
    }

    @Override
    public void connectionClosed() {
        log.info("Connection closed. The current connectionDraining flag is: {}", isConnectionDraining);
        if (isConnectionDraining) {
            reconnect();
        }
    }

    private synchronized void reconnect() {
        log.info("Initiating reconnection ...");
        final BackOffStrategy backoff = new BackOffStrategy(5, 1000);
        while (backoff.shouldRetry()) {
            try {
                connect();
                backoff.doNotRetry();
            } catch (XMPPException | SmackException | IOException | InterruptedException | KeyManagementException
                    | NoSuchAlgorithmException e) {
                log.info("The notifier server could not reconnect after the connection draining message.");
                try {
                    backoff.errorOccured();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Sends a downstream message to FCM with back off strategy
     */
    public void sendMessage(String jsonRequest) throws RetryFailedException {
        final Stanza request = new FcmPacketExtension(jsonRequest).toPacket();
        final BackOffStrategy backoff = new BackOffStrategy();
        while (backoff.shouldRetry()) {
            try {
                xmppConnection.sendStanza(request);
                backoff.doNotRetry();
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                log.info("The packet could not be sent due to a connection problem. Backing off the packet: {}",
                        request.toXML(null));
                backoff.errorOccured();
            }
        }
    }

    public void handleConnectionDraining() {
        log.info("FCM Connection is draining!");
        isConnectionDraining = true;
    }

    public ConnectionStatus getStatus() {
        if (!isConnectionDraining && xmppConnection.isConnected() && xmppConnection.isAuthenticated()) {
            return ConnectionStatus.ACTIVE;
        }
        if (isConnectionDraining && xmppConnection.isConnected()) {
            return ConnectionStatus.DRAINED;
        }
        if (isConnectionDraining && !xmppConnection.isConnected()) {
            return ConnectionStatus.DEAD;
        }
        if (!isConnectionDraining && !xmppConnection.isAuthenticated()) {
            return ConnectionStatus.WAITING;
        }
        return null;
    }

    public void preDestroy() {
        if (xmppConnection.isConnected()) {
            log.info("Disconnecting the xmpp server from FCM.");
            xmppConnection.disconnect();
        }
    }
}
