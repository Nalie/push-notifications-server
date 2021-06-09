package ru.nya.push.service.fcm.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.nya.push.service.fcm.xmpp.backoff.RetryFailedException;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;
import ru.nya.push.service.fcm.xmpp.dto.Message;
import ru.nya.push.service.fcm.xmpp.dto.OutMessage;
import ru.nya.push.service.fcm.xmpp.events.FcmControlReceived;
import ru.nya.push.service.fcm.xmpp.events.MessageDelivered;
import ru.nya.push.service.fcm.xmpp.events.MessageDeliveryFailed;
import ru.nya.push.service.fcm.xmpp.events.UpstreamMessageReceived;
import ru.nya.push.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageManager {

    // downstream messages to sync with acks and nacks
    private final Map<String, Message> syncMessages = new ConcurrentHashMap<>();
    // messages from backoff failures
    private final Map<String, Message> pendingMessages = new ConcurrentHashMap<>();

    private final CcsConnectionManager connectionManager;

    public void sendAsync(OutMessage message) {
        String jsonMessage = JsonUtils.getStringFromObject(message);
        putMessageToSyncMessages(message.getMessageId(), jsonMessage);
        try {
            connectionManager.getConnection().sendMessage(jsonMessage);
        } catch (RetryFailedException e) {
            removeMessageFromSyncMessages(message.getMessageId());
            putMessageToPendingMessages(message.getMessageId(), jsonMessage);
        }
    }

    private void sendAck(String messageId, String to) {
        final Map<String, Object> map = new HashMap<>();
        map.put("message_type", "ack");
        map.put("to", to);
        map.put("message_id", messageId);
        log.info("Sending ack.");
        String jsonMessage = JsonUtils.getStringFromObject(map);
        try {
            connectionManager.getConnection().sendMessage(jsonMessage);
        } catch (RetryFailedException ignored) {
        }
    }

    private void putMessageToSyncMessages(String messageId, String jsonRequest) {
        syncMessages.put(messageId, Message.from(jsonRequest));
    }

    private void putMessageToPendingMessages(String messageId, String jsonRequest) {
        pendingMessages.put(messageId, Message.from(jsonRequest));
    }

    private void removeMessageFromSyncMessages(String messageId) {
        syncMessages.remove(messageId);
    }

    @EventListener
    public void handleMessageDelivered(MessageDelivered messageDelivered) {
        removeMessageFromSyncMessages(messageDelivered.getMessageId());
    }

    @EventListener
    public void handleMessageDeliveryFailed(MessageDeliveryFailed messageDeliveryFailed) {
        removeMessageFromSyncMessages(messageDeliveryFailed.getMessageId());
        final InMessage.ErrorType errorCode = messageDeliveryFailed.getErrorType();
        switch (errorCode) {
            case INVALID_JSON:
            case BAD_REGISTRATION:
            case DEVICE_UNREGISTERED:
            case BAD_ACK:
            case TOPICS_MESSAGE_RATE_EXCEEDED:
            case DEVICE_MESSAGE_RATE_EXCEEDED:
                log.info("Device error: {} -> {}", errorCode, messageDeliveryFailed.getErrorDescription());
                break;
            case SERVICE_UNAVAILABLE:
            case INTERNAL_SERVER_ERROR:
                log.info("Server error: {} -> {}", errorCode, messageDeliveryFailed.getErrorDescription());
                break;
            case CONNECTION_DRAINING:
                log.info("Connection draining from Nack ...");
                connectionManager.getExisted().handleConnectionDraining();
                break;
            default:
                log.info("Received unknown FCM Error Code: {}", errorCode);
                break;
        }
    }

    @EventListener
    public void handleFcmControlReceived(FcmControlReceived fcmControlReceived) {
        InMessage.ControlType controlType = fcmControlReceived.getControlType();
        if (InMessage.ControlType.CONNECTION_DRAINING.equals(controlType)) {
            connectionManager.getExisted().handleConnectionDraining();
        } else {
            log.info("Received unknown FCM Control message: {}", controlType);
        }
    }

    @EventListener
    public void handleMessageReceived(UpstreamMessageReceived messageReceived) {
        // The custom 'action' payload attribute defines what the message action is about.
        InMessage message = messageReceived.getMessage();
        // 1. send ACK to FCM
        sendAck(message.getFrom(), message.getMessageId());
        // 2. process and send message
        //TODO process received upstream message
    }

}
