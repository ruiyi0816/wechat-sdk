package com.wechat.sdk.webhook;

import java.util.List;
import java.util.Objects;

/**
 * 被动回复用户消息 XML 构造器
 * 对应官方文档：消息管理 / 被动回复用户消息 & 转发至客服
 */
public final class PassiveReplyBuilder {

    private PassiveReplyBuilder() {
    }

    /**
     * 构造文本被动回复消息 XML
     */
    public static String replyText(String toUserName, String fromUserName, String content) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return "<xml>\n"
                + "<ToUserName><![CDATA[" + escapeCdata(toUserName) + "]]></ToUserName>\n"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUserName) + "]]></FromUserName>\n"
                + "<CreateTime>" + timestamp + "</CreateTime>\n"
                + "<MsgType><![CDATA[text]]></MsgType>\n"
                + "<Content><![CDATA[" + escapeCdata(content) + "]]></Content>\n"
                + "</xml>";
    }

    /**
     * 针对收到的 WebhookMessage 直接回复文本消息（自动交换 FromUserName 与 ToUserName）
     */
    public static String replyText(WebhookMessage incoming, String content) {
        Objects.requireNonNull(incoming, "incoming message must not be null");
        return replyText(incoming.getFromUserName(), incoming.getToUserName(), content);
    }

    /**
     * 构造图片被动回复消息 XML
     */
    public static String replyImage(String toUserName, String fromUserName, String mediaId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return "<xml>\n"
                + "<ToUserName><![CDATA[" + escapeCdata(toUserName) + "]]></ToUserName>\n"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUserName) + "]]></FromUserName>\n"
                + "<CreateTime>" + timestamp + "</CreateTime>\n"
                + "<MsgType><![CDATA[image]]></MsgType>\n"
                + "<Image><MediaId><![CDATA[" + escapeCdata(mediaId) + "]]></MediaId></Image>\n"
                + "</xml>";
    }

    /**
     * 构造语音被动回复消息 XML
     */
    public static String replyVoice(String toUserName, String fromUserName, String mediaId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return "<xml>\n"
                + "<ToUserName><![CDATA[" + escapeCdata(toUserName) + "]]></ToUserName>\n"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUserName) + "]]></FromUserName>\n"
                + "<CreateTime>" + timestamp + "</CreateTime>\n"
                + "<MsgType><![CDATA[voice]]></MsgType>\n"
                + "<Voice><MediaId><![CDATA[" + escapeCdata(mediaId) + "]]></MediaId></Voice>\n"
                + "</xml>";
    }

    /**
     * 构造视频被动回复消息 XML
     */
    public static String replyVideo(String toUserName, String fromUserName, String mediaId, String title, String description) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return "<xml>\n"
                + "<ToUserName><![CDATA[" + escapeCdata(toUserName) + "]]></ToUserName>\n"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUserName) + "]]></FromUserName>\n"
                + "<CreateTime>" + timestamp + "</CreateTime>\n"
                + "<MsgType><![CDATA[video]]></MsgType>\n"
                + "<Video>\n"
                + "<MediaId><![CDATA[" + escapeCdata(mediaId) + "]]></MediaId>\n"
                + "<Title><![CDATA[" + escapeCdata(title) + "]]></Title>\n"
                + "<Description><![CDATA[" + escapeCdata(description) + "]]></Description>\n"
                + "</Video>\n"
                + "</xml>";
    }

    /**
     * 构造音乐被动回复消息 XML
     */
    public static String replyMusic(
            String toUserName,
            String fromUserName,
            String title,
            String description,
            String musicUrl,
            String hqMusicUrl,
            String thumbMediaId
    ) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return "<xml>\n"
                + "<ToUserName><![CDATA[" + escapeCdata(toUserName) + "]]></ToUserName>\n"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUserName) + "]]></FromUserName>\n"
                + "<CreateTime>" + timestamp + "</CreateTime>\n"
                + "<MsgType><![CDATA[music]]></MsgType>\n"
                + "<Music>\n"
                + "<Title><![CDATA[" + escapeCdata(title) + "]]></Title>\n"
                + "<Description><![CDATA[" + escapeCdata(description) + "]]></Description>\n"
                + "<MusicUrl><![CDATA[" + escapeCdata(musicUrl) + "]]></MusicUrl>\n"
                + "<HQMusicUrl><![CDATA[" + escapeCdata(hqMusicUrl) + "]]></HQMusicUrl>\n"
                + "<ThumbMediaId><![CDATA[" + escapeCdata(thumbMediaId) + "]]></ThumbMediaId>\n"
                + "</Music>\n"
                + "</xml>";
    }

    /**
     * 构造图文被动回复消息 XML
     */
    public static String replyNews(String toUserName, String fromUserName, List<ArticleItem> articles) {
        Objects.requireNonNull(articles, "articles must not be null");
        long timestamp = System.currentTimeMillis() / 1000L;
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>\n")
                .append("<ToUserName><![CDATA[").append(escapeCdata(toUserName)).append("]]></ToUserName>\n")
                .append("<FromUserName><![CDATA[").append(escapeCdata(fromUserName)).append("]]></FromUserName>\n")
                .append("<CreateTime>").append(timestamp).append("</CreateTime>\n")
                .append("<MsgType><![CDATA[news]]></MsgType>\n")
                .append("<ArticleCount>").append(articles.size()).append("</ArticleCount>\n")
                .append("<Articles>\n");
        for (ArticleItem item : articles) {
            sb.append("<item>\n")
                    .append("<Title><![CDATA[").append(escapeCdata(item.title())).append("]]></Title>\n")
                    .append("<Description><![CDATA[").append(escapeCdata(item.description())).append("]]></Description>\n")
                    .append("<PicUrl><![CDATA[").append(escapeCdata(item.picUrl())).append("]]></PicUrl>\n")
                    .append("<Url><![CDATA[").append(escapeCdata(item.url())).append("]]></Url>\n")
                    .append("</item>\n");
        }
        sb.append("</Articles>\n</xml>");
        return sb.toString();
    }

    /**
     * 将消息转发到客服系统 (transfer_customer_service)
     *
     * @param kfAccount 指定接入的客服账号（可为 null，表示不指定客服）
     */
    public static String transferToCustomerService(String toUserName, String fromUserName, String kfAccount) {
        long timestamp = System.currentTimeMillis() / 1000L;
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>\n")
                .append("<ToUserName><![CDATA[").append(escapeCdata(toUserName)).append("]]></ToUserName>\n")
                .append("<FromUserName><![CDATA[").append(escapeCdata(fromUserName)).append("]]></FromUserName>\n")
                .append("<CreateTime>").append(timestamp).append("</CreateTime>\n")
                .append("<MsgType><![CDATA[transfer_customer_service]]></MsgType>\n");
        if (kfAccount != null && !kfAccount.isBlank()) {
            sb.append("<TransInfo><KfAccount><![CDATA[").append(escapeCdata(kfAccount)).append("]]></KfAccount></TransInfo>\n");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    private static String escapeCdata(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("]]>", "]]]]><![CDATA[>");
    }

    public record ArticleItem(String title, String description, String picUrl, String url) {
    }
}
