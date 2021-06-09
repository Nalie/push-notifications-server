package ru.nya.push.service.fcm.xmpp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Represents an incoming message from FCM CCS
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InMessage {

    // For the app common payload message attributes (android - xmpp server)
    public static final String PAYLOAD_ATTRIBUTE_MESSAGE = "message";
    public static final String PAYLOAD_ATTRIBUTE_ACTION = "action";
    public static final String PAYLOAD_ATTRIBUTE_RECIPIENT = "recipient";

    // Sender registration ID
    private String from;
    // Sender app's package
    private String category;
    // Unique id for this message
    @JsonProperty("message_id")
    private String messageId;
    @JsonProperty("time_to_live")
    private String ttl;
    // Payload data. A String in JSON format
    private Map<String, String> data;

    //for ack and nack
    @JsonProperty("message_type")
    private MessageType messageType;
    @JsonProperty("control_type")
    private ControlType controlType;
    @JsonProperty("error")
    private ErrorType error;
    @JsonProperty("error_description")
    private String errorDescription;

    public enum ErrorType {
        INVALID_JSON, BAD_REGISTRATION, DEVICE_UNREGISTERED, SENDER_ID_MISMATCH,
        BAD_ACK, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR, DEVICE_MESSAGE_RATE_EXCEEDED,
        TOPICS_MESSAGE_RATE_EXCEEDED, CONNECTION_DRAINING, INVALID_APNS_CREDENTIAL, AUTHENTICATION_FAILED
    }

    public enum MessageType {
        ack, nack, control
    }

    public enum ControlType {
        CONNECTION_DRAINING
    }
}
