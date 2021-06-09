package ru.nya.push.service.fcm.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.nya.push.service.fcm.xmpp.FcmPacketExtension;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;
import ru.nya.push.service.fcm.xmpp.events.FcmControlReceived;
import ru.nya.push.service.fcm.xmpp.events.MessageDelivered;
import ru.nya.push.service.fcm.xmpp.events.MessageDeliveryFailed;
import ru.nya.push.service.fcm.xmpp.events.UpstreamMessageReceived;
import ru.nya.push.util.JsonUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageReceiver implements StanzaListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void processStanza(Stanza packet) {
        log.info("Processing packet in thread {} - {}", Thread.currentThread().getName(), Thread.currentThread().getId());
        log.info("Received: {}", packet.toXML(null));
        final FcmPacketExtension fcmPacket = (FcmPacketExtension) packet.getExtension(FcmPacketExtension.FCM_NAMESPACE);
        final String json = fcmPacket.getJson();
        Optional<InMessage> messageObject = Optional.ofNullable(JsonUtils.getObjectFromString(json, InMessage.class));
        if (messageObject.isEmpty()) {
            log.info("Error parsing Packet JSON to JSON String: {}", json);
            return;
        }
        InMessage message = messageObject.get();
        final Optional<InMessage.MessageType> messageTypeObj = Optional.ofNullable(message.getMessageType());

        if (messageTypeObj.isEmpty()) {
            // upstream message from a device client
            handleUpstreamMessage(message);
            return;
        }
        final InMessage.MessageType messageType = messageTypeObj.get();
        switch (messageType) {
            case ack:
                handleAckReceipt(message);
                break;
            case nack:
                handleNackReceipt(message);
                break;
            case control:
                handleControlMessage(message);
                break;
            default:
                log.info("Received unknown FCM message type: {}", messageType);
        }
    }

    /**
     * Handles an upstream message from a device client through FCM
     */
    private void handleUpstreamMessage(InMessage message) {
        UpstreamMessageReceived messageReceived = new UpstreamMessageReceived(this, message);
        applicationEventPublisher.publishEvent(messageReceived);
    }

    /**
     * Handles an ACK message from FCM
     */
    private void handleAckReceipt(InMessage message) {
        MessageDelivered delivered = new MessageDelivered(this, message.getMessageId());
        applicationEventPublisher.publishEvent(delivered);
    }

    /**
     * Handles a NACK message from FCM
     */
    private void handleNackReceipt(InMessage message) {
        MessageDeliveryFailed deliveryFailed = new MessageDeliveryFailed(this, message.getMessageId(),
                message.getError(), message.getErrorDescription());
        applicationEventPublisher.publishEvent(deliveryFailed);
    }

    /**
     * Handles a Control message from FCM
     */
    private void handleControlMessage(InMessage message) {
        FcmControlReceived control = new FcmControlReceived(this, message.getControlType());
        applicationEventPublisher.publishEvent(control);
    }
}
