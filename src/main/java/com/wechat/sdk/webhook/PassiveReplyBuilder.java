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
        return """
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[text]]></MsgType>
                <Content><![CDATA[%s]]></Content>
                </xml>""".formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp, escapeCdata(content));
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
        return """
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[image]]></MsgType>
                <Image><MediaId><![CDATA[%s]]></MediaId></Image>
                </xml>""".formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp, escapeCdata(mediaId));
    }

    /**
     * 构造语音被动回复消息 XML
     */
    public static String replyVoice(String toUserName, String fromUserName, String mediaId) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return """
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[voice]]></MsgType>
                <Voice><MediaId><![CDATA[%s]]></MediaId></Voice>
                </xml>""".formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp, escapeCdata(mediaId));
    }

    /**
     * 构造视频被动回复消息 XML
     */
    public static String replyVideo(String toUserName, String fromUserName, String mediaId, String title, String description) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return """
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[video]]></MsgType>
                <Video>
                <MediaId><![CDATA[%s]]></MediaId>
                <Title><![CDATA[%s]]></Title>
                <Description><![CDATA[%s]]></Description>
                </Video>
                </xml>""".formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp,
                        escapeCdata(mediaId), escapeCdata(title), escapeCdata(description));
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
        return """
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[music]]></MsgType>
                <Music>
                <Title><![CDATA[%s]]></Title>
                <Description><![CDATA[%s]]></Description>
                <MusicUrl><![CDATA[%s]]></MusicUrl>
                <HQMusicUrl><![CDATA[%s]]></HQMusicUrl>
                <ThumbMediaId><![CDATA[%s]]></ThumbMediaId>
                </Music>
                </xml>""".formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp,
                        escapeCdata(title), escapeCdata(description), escapeCdata(musicUrl),
                        escapeCdata(hqMusicUrl), escapeCdata(thumbMediaId));
    }

    /**
     * 构造图文被动回复消息 XML
     */
    public static String replyNews(String toUserName, String fromUserName, List<ArticleItem> articles) {
        Objects.requireNonNull(articles, "articles must not be null");
        long timestamp = System.currentTimeMillis() / 1000L;
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[news]]></MsgType>
                <ArticleCount>%d</ArticleCount>
                <Articles>
                """.formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp, articles.size()));
        for (ArticleItem item : articles) {
            sb.append("""
                    <item>
                    <Title><![CDATA[%s]]></Title>
                    <Description><![CDATA[%s]]></Description>
                    <PicUrl><![CDATA[%s]]></PicUrl>
                    <Url><![CDATA[%s]]></Url>
                    </item>
                    """.formatted(
                            escapeCdata(item.title()), escapeCdata(item.description()),
                            escapeCdata(item.picUrl()), escapeCdata(item.url())));
        }
        sb.append("""
                </Articles>
                </xml>""");
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
        sb.append("""
                <xml>
                <ToUserName><![CDATA[%s]]></ToUserName>
                <FromUserName><![CDATA[%s]]></FromUserName>
                <CreateTime>%d</CreateTime>
                <MsgType><![CDATA[transfer_customer_service]]></MsgType>
                """.formatted(
                        escapeCdata(toUserName), escapeCdata(fromUserName), timestamp));
        if (kfAccount != null && !kfAccount.isBlank()) {
            sb.append("""
                    <TransInfo><KfAccount><![CDATA[%s]]></KfAccount></TransInfo>
                    """.formatted(escapeCdata(kfAccount)));
        }
        sb.append("""
                </xml>""");
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
