package ru.nya.push.service.fcm.xmpp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class MessageDelivered extends ApplicationEvent {

    @Getter
    private final String messageId;

    public MessageDelivered(Object source, String messageId) {
        super(source);
        this.messageId = messageId;
    }
}
