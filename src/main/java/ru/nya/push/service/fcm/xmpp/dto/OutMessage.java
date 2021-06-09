package ru.nya.push.service.fcm.xmpp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
/*
https://firebase.google.com/docs/cloud-messaging/xmpp-server-ref#notification-payload-support
* */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutMessage {

    // Sender registration ID
    private final String to;
    // Condition that determines the message target
    private final String condition;
    // Unique id for this message
    @JsonProperty("message_id")
    private final String messageId;
    // Identifies a group of messages
    @JsonProperty("collapse_key")
    private final String collapseKey;
    // Priority of the message
    private final String priority;
    // Flag to wake client devices
    @JsonProperty("content_available")
    private final Boolean contentAvailable;
    @JsonProperty("mutable_content")
    private final String mutableContent;
    // Time to live
    @JsonProperty("time_to_live")
    private final Integer timeToLive;
    // Test request without sending a message
    @JsonProperty("dry_run")
    private final Boolean dryRun;
    // Payload data. A String in JSON format
    private final Map<String, String> data;
    // Payload notification. A String in JSON format
    private final Notification notification;
}
