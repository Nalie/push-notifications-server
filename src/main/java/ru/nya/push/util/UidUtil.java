package ru.nya.push.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class UidUtil {

    /**
     * Returns a random message id to uniquely identify a message
     */
    public static String getUniqueMessageId() {
        LocalDateTime now = LocalDateTime.now();
        final String formatted = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
        UUID randomUUID = UUID.randomUUID();
        return "m-" + formatted + "-" + randomUUID;
    }
}
