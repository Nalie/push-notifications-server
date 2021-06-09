package ru.nya.push.service.fcm.xmpp;

import org.junit.jupiter.api.Test;

class FcmPacketExtensionTest {

    @Test
    void toXML() {
        String xml = new FcmPacketExtension("test").toXML("test");
        assert xml.equals("<gcm xmlns='google:mobile:data'>test</gcm>");
    }
}