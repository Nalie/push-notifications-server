package ru.nya.push.service.fcm.xmpp.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class Notification {
    @JsonProperty("title")
    private final String title;
    @JsonProperty("body")
    private final String body;
    @JsonProperty("sound")
    private final String sound;
    @JsonProperty("badge")
    private final String badge;
    @JsonProperty("click_action")
    private final String clickAction;
    @JsonProperty("subtitle")
    private final String subtitle;
    @JsonProperty("body_loc_key")
    private final String bodyLocKey;
    @JsonProperty("body_loc_args")
    private final String bodyLocArgs;
    @JsonProperty("title_loc_key")
    private final String titleLocKey;
    @JsonProperty("title_loc_args")
    private final String titleLocArgs;
    @JsonProperty("android_channel_id")
    private final String androidChannelId;
    @JsonProperty("icon")
    private final String icon;
    @JsonProperty("tag")
    private final String tag;
    @JsonProperty("color")
    private final String color;
}
