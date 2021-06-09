package ru.nya.push.service.fcm.xmpp.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a message for the sync and pending list
 */
@Getter
@Setter
@EqualsAndHashCode
public class Message {

    private Long timestamp; // in millis
    private String jsonRequest;

    public static Message from(String jsonRequest) {
        return new Message(System.currentTimeMillis(), jsonRequest);
    }

    private Message(Long timestamp, String jsonRequest) {
        this.timestamp = timestamp;
        this.jsonRequest = jsonRequest;
    }
}
