package ru.nya.push.service.fcm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fcm")
@Component
@Getter
@Setter
public class FcmSettings {
    private String serviceAccountFile;
    private String projectSenderId;
    private String serverKey;
}
