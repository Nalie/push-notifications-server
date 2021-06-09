package ru.nya.push.service.fcm.xmpp.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;

@Getter
public class MessageDeliveryFailed extends ApplicationEvent {
    
    private final String messageId;
    private final InMessage.ErrorType errorType;
    private final String errorDescription;

    public MessageDeliveryFailed(Object source, String messageId, InMessage.ErrorType errorType, String errorDescription) {
        super(source);
        this.messageId = messageId;
        this.errorType = errorType;
        this.errorDescription = errorDescription;
    }
}
