package ru.nya.push.service.fcm.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nya.push.service.fcm.dto.PushNotification;
import ru.nya.push.service.fcm.xmpp.dto.Notification;
import ru.nya.push.service.fcm.xmpp.dto.OutMessage;
import ru.nya.push.util.UidUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class XmppFcmService {

    private final MessageManager messageManager;

    public void sendPersonal(PushNotification pushNotification, String clientToken) {
        final String messageId = UidUtil.getUniqueMessageId();
        Map<String, String> data = new HashMap<>();
        data.put("message", pushNotification.getBody());
        OutMessage message = OutMessage.builder()
                .to(clientToken)
                .messageId(messageId)
                .timeToLive(pushNotification.getTtlInSeconds())
                .notification(Notification.builder()
                        .title(pushNotification.getTitle())
                        .body(pushNotification.getBody())
                        .icon(pushNotification.getIcon())
                        .clickAction(pushNotification.getClickAction())
                        .build())
                .data(data)//TODO
                .build();
        messageManager.sendAsync(message);
    }

}
