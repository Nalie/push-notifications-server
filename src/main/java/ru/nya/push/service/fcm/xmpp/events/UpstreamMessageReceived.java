package ru.nya.push.service.fcm.xmpp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;

public class UpstreamMessageReceived extends ApplicationEvent {

    @Getter
    private final InMessage message;

    public UpstreamMessageReceived(Object source, InMessage message) {
        super(source);
        this.message = message;
    }
}
