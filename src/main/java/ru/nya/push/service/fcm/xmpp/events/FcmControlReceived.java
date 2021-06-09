package ru.nya.push.service.fcm.xmpp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;

@Getter
public class FcmControlReceived extends ApplicationEvent {

    private final InMessage.ControlType controlType;

    public FcmControlReceived(Object source, InMessage.ControlType controlType) {
        super(source);
        this.controlType = controlType;
    }
}
