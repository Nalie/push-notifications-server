package ru.nya.push.service.fcm.http;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;
import ru.nya.push.service.fcm.FcmSettings;
import ru.nya.push.service.fcm.dto.PushNotification;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class HttpFcmService {

    public HttpFcmService(FcmSettings settings) {
        try (InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream(settings.getServiceAccountFile())) {
            assert serviceAccount != null;
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            Logger.getLogger(HttpFcmService.class.getName())
                    .log(Level.SEVERE, null, e);
        }
    }

    public String sendByTopic(PushNotification conf, String topic) throws InterruptedException, ExecutionException {
        Message message = Message.builder().setTopic(topic)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds() == null ? "": conf.getTtlInSeconds().toString())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();

        return FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
    }

    public String sendPersonal(PushNotification conf, String clientToken) throws ExecutionException, InterruptedException {
        Message message = Message.builder().setToken(clientToken)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("ttl", conf.getTtlInSeconds() == null ? "": conf.getTtlInSeconds().toString())
                        .setNotification(createBuilder(conf).build())
                        .build())
                .build();

        return FirebaseMessaging.getInstance()
                .sendAsync(message)
                .get();
    }

    public void subscribeUsers(String topic, List<String> clientTokens) throws FirebaseMessagingException {
        for (String token : clientTokens) {
            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(Collections.singletonList(token), topic);
        }
    }

    private WebpushNotification.Builder createBuilder(PushNotification conf) {
        WebpushNotification.Builder builder = WebpushNotification.builder();
        builder.addAction(new WebpushNotification
                .Action(conf.getClickAction(), "Открыть"))
                .setImage(conf.getIcon())
                .setTitle(conf.getTitle())
                .setBody(conf.getBody());
        return builder;
    }
}
