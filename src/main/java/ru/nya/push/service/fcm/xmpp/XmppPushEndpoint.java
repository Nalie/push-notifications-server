package ru.nya.push.service.fcm.xmpp;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import ru.nya.push.service.fcm.dto.PushNotificationRequest;
import ru.nya.push.service.fcm.http.HttpFcmService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Path("/xmpp")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Controller
public class XmppPushEndpoint {

    private final XmppFcmService fcmService;

    @PUT
    @Path("/push")
    public void push(@NotNull PushNotificationRequest pushNotificationRequest) throws ExecutionException, InterruptedException {
        fcmService.sendPersonal(pushNotificationRequest.getPushNotification(), pushNotificationRequest.getClientToken());
    }

}
