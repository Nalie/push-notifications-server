package ru.nya.push.util;

import org.junit.jupiter.api.Test;
import ru.nya.push.service.fcm.xmpp.dto.InMessage;

class JsonUtilsTest {

    @Test
    void getObjectFromString() {
        InMessage objectFromString = JsonUtils.getObjectFromString("{\"message_type\":\"ack\",\"from\":\"fXoLVsrGtgk:APA91bHr7KXTs2zITyeDj3LkidrefDQAelG0TVEy2-TrhOk_s--nfva88CmsyWaod876VOQNeqRdHcTkk1dXdK7yCh7nIuJScwIB2_eluFDVDwLLKOAebFkh35ooACxY9SvdZpYatVKl\",\"message_id\":\"m-20210530081720-f3fd115e-8dec-4481-846d-a72d09c3e6d7\"}",
                InMessage.class);
        assert objectFromString.getMessageType().equals(InMessage.MessageType.ack);
    }
}