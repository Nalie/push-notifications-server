package ru.nya.push;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import ru.nya.push.service.fcm.http.HttpPushEndpoint;
import ru.nya.push.service.fcm.xmpp.XmppPushEndpoint;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

/**
 * @author yapparova-nv on 14.02.2017.
 */
@ApplicationPath(RestApp.PUB_REST_PATH)
@Component
public class RestApp extends ResourceConfig {
    public static final String PUB_REST_PATH = "/rest";

    @Inject
    public RestApp() {
        registerEndpoints();
    }

    private void registerEndpoints() {
        register(HttpPushEndpoint.class);
        register(XmppPushEndpoint.class);
    }
}
