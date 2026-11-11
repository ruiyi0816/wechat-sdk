package com.wechat.sdk.webhook;

import com.wechat.sdk.exception.WeChatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信回调推送消息与事件统一载体（支持普通消息与各类事件推送）
 * 对应官方文档：接收普通消息 / 接收事件推送
 */
public record WebhookMessage(Map<String, String> rawFields) {

    public WebhookMessage {
        rawFields = rawFields != null ? Collections.unmodifiableMap(new LinkedHashMap<>(rawFields)) : Map.of();
    }

    /**
     * 从明文 XML 字符串解析为 WebhookMessage
     */
    public static WebhookMessage fromXml(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new WeChatException("Webhook XML payload must not be empty");
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml.trim())));
            Element root = doc.getDocumentElement();
            NodeList children = root.getChildNodes();
            Map<String, String> fields = new LinkedHashMap<>();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    fields.put(node.getNodeName(), node.getTextContent());
                }
            }
            return new WebhookMessage(fields);
        } catch (Exception e) {
            throw new WeChatException("Failed to parse WeChat webhook XML payload", e);
        }
    }

    public String getField(String tagName) {
        return rawFields.get(tagName);
    }

    public Map<String, String> getRawFields() {
        return rawFields;
    }

    public String getToUserName() {
        return rawFields.get("ToUserName");
    }

    public String getFromUserName() {
        return rawFields.get("FromUserName");
    }

    public Long getCreateTime() {
        return parseLong(rawFields.get("CreateTime"));
    }

    public String getMsgType() {
        return rawFields.get("MsgType");
    }

    public Long getMsgId() {
        String id = rawFields.get("MsgId");
        if (id == null) {
            id = rawFields.get("MsgID");
        }
        return parseLong(id);
    }

    public String getContent() {
        return rawFields.get("Content");
    }

    public String getPicUrl() {
        return rawFields.get("PicUrl");
    }

    public String getMediaId() {
        return rawFields.get("MediaId");
    }

    public String getFormat() {
        return rawFields.get("Format");
    }

    public String getRecognition() {
        return rawFields.get("Recognition");
    }

    public String getThumbMediaId() {
        return rawFields.get("ThumbMediaId");
    }

    public Double getLocationX() {
        return parseDouble(rawFields.get("Location_X"));
    }

    public Double getLocationY() {
        return parseDouble(rawFields.get("Location_Y"));
    }

    public Integer getScale() {
        return parseInt(rawFields.get("Scale"));
    }

    public String getLabel() {
        return rawFields.get("Label");
    }

    public String getTitle() {
        return rawFields.get("Title");
    }

    public String getDescription() {
        return rawFields.get("Description");
    }

    public String getUrl() {
        return rawFields.get("Url");
    }

    public String getEvent() {
        return rawFields.get("Event");
    }

    public String getEventKey() {
        return rawFields.get("EventKey");
    }

    public String getTicket() {
        return rawFields.get("Ticket");
    }

    public Double getLatitude() {
        return parseDouble(rawFields.get("Latitude"));
    }

    public Double getLongitude() {
        return parseDouble(rawFields.get("Longitude"));
    }

    public Double getPrecision() {
        return parseDouble(rawFields.get("Precision"));
    }

    public String getStatus() {
        return rawFields.get("Status");
    }

    /**
     * 生成用于消息去重的唯一键（普通消息使用 MsgId，事件消息使用 FromUserName + "_" + CreateTime + "_" + Event）
     */
    public String getDeduplicationKey() {
        Long msgId = getMsgId();
        if (msgId != null) {
            return "msgid:" + msgId;
        }
        return "event:" + getFromUserName() + ":" + getCreateTime() + ":" + getEvent() + ":" + getEventKey();
    }

    private static Long parseLong(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "WebhookMessage" + rawFields;
    }
}
