package ru.nya.push.service.fcm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushNotificationRequest {

    @JsonProperty("to")
    private String clientToken;

    @JsonProperty("notification")
    private PushNotification pushNotification;
}
