package ru.nya.push.service.fcm.xmpp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;

@Getter
public class ConnectionIsReady extends ApplicationEvent {

    public ConnectionIsReady(Object source) {
        super(source);
    }
}
