package ru.nya.push.service.fcm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
public class PushNotification {

    private String title;
    private String body;
    private String icon;
    @JsonProperty("click_action")
    private String clickAction;
    private Integer ttlInSeconds;
}
