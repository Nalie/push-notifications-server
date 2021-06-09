package ru.nya.push.service.fcm.xmpp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * XMPP Packet Extension for FCM Cloud Connection Server
 */
@RequiredArgsConstructor
public class FcmPacketExtension implements ExtensionElement {

    public static final String FCM_ELEMENT_NAME = "gcm";
    public static final String FCM_NAMESPACE = "google:mobile:data";

    @Getter
    private final String json;

    @Override
    public String toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();
        xml.append(StringUtils.escapeForXml(json));
        xml.closeElement(getElementName());
        return xml.toString();
        // TODO: 1. Do we need to scape the json? StringUtils.escapeForXML(json) 2. How to use the enclosing namespace?
    }

    @Override
    public String getElementName() {
        return FCM_ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return FCM_NAMESPACE;
    }

    public Stanza toPacket() {
        final Message message = new Message();
        message.addExtension(this);
        return message;
    }
}
