package com.wechat.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.config.WeChatConfig;
import com.wechat.sdk.crypto.WeChatCryptUtil;
import com.wechat.sdk.crypto.WeChatSignatureUtil;
import com.wechat.sdk.exception.WeChatApiException;
import com.wechat.sdk.http.HttpTransport;
import com.wechat.sdk.http.HttpTransport.RawHttpRequest;
import com.wechat.sdk.http.HttpTransport.RawHttpResponse;
import com.wechat.sdk.http.MediaDownloadResult;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.account.QrCodeTicket;
import com.wechat.sdk.model.account.ShortenFetchResponse;
import com.wechat.sdk.model.base.NetworkCheckResponse;
import com.wechat.sdk.model.base.QuotaResponse;
import com.wechat.sdk.model.base.RidInfoResponse;
import com.wechat.sdk.model.material.DraftArticle;
import com.wechat.sdk.model.material.MaterialCountResponse;
import com.wechat.sdk.model.material.MediaUploadResponse;
import com.wechat.sdk.model.menu.Menu;
import com.wechat.sdk.model.menu.MenuButton;
import com.wechat.sdk.model.menu.MenuGetResponse;
import com.wechat.sdk.model.message.CustomMessageRequest;
import com.wechat.sdk.model.message.SubscribeMessageRequest;
import com.wechat.sdk.model.message.TemplateMessageRequest;
import com.wechat.sdk.model.oauth.JsApiSignature;
import com.wechat.sdk.model.oauth.OAuthAccessToken;
import com.wechat.sdk.model.user.UserInfo;
import com.wechat.sdk.model.user.UserListResponse;
import com.wechat.sdk.model.user.UserTag;
import com.wechat.sdk.token.DefaultAccessTokenManager;
import com.wechat.sdk.webhook.PassiveReplyBuilder;
import com.wechat.sdk.webhook.WeChatMessageRouter;
import com.wechat.sdk.webhook.WebhookMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 微信公众号（服务号）SDK 全链路单元测试
 */
class WeChatSdkTest {

    private static final String TEST_APP_ID = "wx1234567890abcdef";
    private static final String TEST_APP_SECRET = "secret_9876543210";
    private static final String TEST_TOKEN = "my_webhook_token";
    private static final String TEST_AES_KEY = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";

    private final Map<String, Function<RawHttpRequest, RawHttpResponse>> mockRoutes = new ConcurrentHashMap<>();
    private final AtomicInteger tokenFetchCount = new AtomicInteger(0);
    private WeChatClient client;

    @BeforeEach
    void setUp() {
        tokenFetchCount.set(0);
        mockRoutes.clear();

        // 1. AccessToken 接口
        registerJson("/cgi-bin/token", req -> {
            int count = tokenFetchCount.incrementAndGet();
            return "{\"access_token\":\"mock_token_" + count + "\",\"expires_in\":7200}";
        });

        // 2. 稳定版 AccessToken 接口
        registerJson("/cgi-bin/stable_token", req -> {
            int count = tokenFetchCount.incrementAndGet();
            return "{\"access_token\":\"stable_token_" + count + "\",\"expires_in\":7200}";
        });

        // 3. 基础与 OpenAPI 接口
        registerJson("/cgi-bin/get_api_domain_ip", req ->
                "{\"ip_list\":[\"101.226.103.0/25\",\"140.207.54.0/25\"]}");
        registerJson("/cgi-bin/callback/check", req ->
                "{\"dns\":[{\"ip\":\"111.161.64.40\",\"real_operator\":\"UNICOM\"}],\"ping\":[{\"ip\":\"111.161.64.40\",\"from_operator\":\"UNICOM\",\"package_loss\":\"0%\",\"time\":\"20.5ms\"}]}");
        registerJson("/cgi-bin/openapi/quota/get", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"quota\":{\"daily_limit\":500000,\"used\":120,\"remain\":499880}}");
        registerJson("/cgi-bin/openapi/rid/get", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"request\":{\"invoke_time\":1700000000,\"cost_in_ms\":35,\"request_url\":\"/cgi-bin/menu/create\",\"client_ip\":\"127.0.0.1\"}}");

        // 4. 菜单接口
        registerJson("/cgi-bin/menu/create", req -> "{\"errcode\":0,\"errmsg\":\"ok\"}");
        registerJson("/cgi-bin/menu/get", req ->
                "{\"menu\":{\"button\":[{\"type\":\"click\",\"name\":\"今日歌曲\",\"key\":\"V1001_TODAY_MUSIC\"}]}}");

        // 5. 消息接口（客服、模板、订阅通知）
        registerJson("/cgi-bin/message/custom/send", req -> "{\"errcode\":0,\"errmsg\":\"ok\"}");
        registerJson("/cgi-bin/message/template/send", req -> "{\"errcode\":0,\"errmsg\":\"ok\",\"msgid\":200228332}");
        registerJson("/cgi-bin/message/subscribe/bizsend", req -> "{\"errcode\":0,\"errmsg\":\"ok\"}");

        // 6. 素材、草稿与发布接口
        registerJson("/cgi-bin/media/upload", req -> {
            String contentType = req.headers().get("Content-Type");
            assertTrue(contentType != null && contentType.startsWith("multipart/form-data"));
            return "{\"type\":\"image\",\"media_id\":\"MEDIA_ID_001\",\"created_at\":1700000000}";
        });
        mockRoutes.put("/cgi-bin/media/get", req -> new RawHttpResponse(
                200,
                Map.of(
                        "Content-Type", List.of("image/jpeg"),
                        "Content-Disposition", List.of("attachment; filename=\"test.jpg\"")
                ),
                new byte[]{1, 2, 3, 4, 5}
        ));
        registerJson("/cgi-bin/material/get_materialcount", req ->
                "{\"voice_count\":1,\"video_count\":2,\"image_count\":10,\"news_count\":5}");
        registerJson("/cgi-bin/draft/add", req -> "{\"media_id\":\"DRAFT_MEDIA_999\"}");
        registerJson("/cgi-bin/freepublish/submit", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"publish_id\":\"100000001\"}");

        // 7. 用户管理接口
        registerJson("/cgi-bin/tags/create", req -> "{\"tag\":{\"id\":134,\"name\":\"广东\"}}");
        registerJson("/cgi-bin/user/info", req ->
                "{\"subscribe\":1,\"openid\":\"o6_bmjrPTlm6_2sgVt7hMZOPfL2M\",\"language\":\"zh_CN\",\"subscribe_time\":1700000000,\"unionid\":\"o6_bmasdasdsad6_2sgVt7hMZOPfL\"}");
        registerJson("/cgi-bin/user/get", req ->
                "{\"total\":2,\"count\":2,\"data\":{\"openid\":[\"OPENID1\",\"OPENID2\"]},\"next_openid\":\"OPENID2\"}");

        // 8. 账号二维码与短 Key 接口
        registerJson("/cgi-bin/qrcode/create", req ->
                "{\"ticket\":\"gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw==\",\"expire_seconds\":60,\"url\":\"http://weixin.qq.com/q/kZgfwMTm72WWPkovabbI\"}");
        registerJson("/cgi-bin/shorten/gen", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"short_key\":\"key_abc123\"}");
        registerJson("/cgi-bin/shorten/fetch", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"long_data\":\"https://example.com/very/long/payload\",\"create_time\":1700000000,\"expire_seconds\":3600}");

        // 9. OAuth2 & JS-SDK Ticket
        registerJson("/sns/oauth2/access_token", req ->
                "{\"access_token\":\"OAUTH_TOKEN\",\"expires_in\":7200,\"refresh_token\":\"REFRESH_TOKEN\",\"openid\":\"OPENID_USER\",\"scope\":\"snsapi_userinfo\"}");
        registerJson("/cgi-bin/ticket/getticket", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"ticket\":\"sM4AOVdWfPE4DxkXGEs8VMCPGGVi4C3VM0P37wVUCFvkVAy_90u5h9nbSlYy3-Sl-HhTdfl2fzFy1AOcHKP7qg\",\"expires_in\":7200}");

        // 10. 数据统计、智能接口、留言与多客服接口
        registerJson("/datacube/getusersummary", req ->
                "{\"list\":[{\"ref_date\":\"2026-11-01\",\"user_source\":0,\"new_user\":15,\"cancel_user\":2}]}");
        registerJson("/cv/ocr/idcard", req ->
                "{\"errcode\":0,\"errmsg\":\"ok\",\"type\":\"Front\",\"name\":\"张三\"}");
        registerJson("/cgi-bin/comment/open", req -> "{\"errcode\":0,\"errmsg\":\"ok\"}");
        registerJson("/customservice/kfaccount/add", req -> "{\"errcode\":0,\"errmsg\":\"ok\"}");

        WeChatConfig config = WeChatConfig.builder()
                .appId(TEST_APP_ID)
                .appSecret(TEST_APP_SECRET)
                .token(TEST_TOKEN)
                .encodingAesKey(TEST_AES_KEY)
                .build();

        HttpTransport mockTransport = req -> {
            String path = req.uri().getPath();
            Function<RawHttpRequest, RawHttpResponse> handler = mockRoutes.get(path);
            assertNotNull(handler, "No mock route registered for path: " + path);
            return handler.apply(req);
        };

        WeChatHttpClient httpClient = new WeChatHttpClient(config, mockTransport);
        DefaultAccessTokenManager tokenManager = new DefaultAccessTokenManager(config, httpClient);
        client = new WeChatClient(config, httpClient, tokenManager);
    }

    @Test
    void testAccessTokenCachingAndAutoRetryOnExpired() {
        String token1 = client.base().getAccessToken();
        String token2 = client.base().getAccessToken();
        assertEquals("mock_token_1", token1);
        assertEquals(token1, token2);
        assertEquals(1, tokenFetchCount.get(), "Token should be cached in memory");

        // 模拟某个接口第一次返回 40001 (access_token invalid)，第二次成功
        AtomicInteger callAttempt = new AtomicInteger(0);
        registerJson("/cgi-bin/getcallbackip", req -> {
            if (callAttempt.incrementAndGet() == 1) {
                return "{\"errcode\":40001,\"errmsg\":\"invalid credential, access_token is invalid\",\"rid\":\"rid_001\"}";
            }
            return "{\"ip_list\":[\"101.226.62.77\"]}";
        });

        List<String> callbackIps = client.base().getCallbackIp();
        assertEquals(List.of("101.226.62.77"), callbackIps);
        assertEquals(2, callAttempt.get());
        assertEquals(2, tokenFetchCount.get(), "Should have automatically refreshed access_token after 40001");
        assertEquals("mock_token_2", client.base().getAccessToken());
    }

    @Test
    void testBaseAndOpenApiService() {
        List<String> ips = client.base().getApiDomainIp();
        assertEquals(2, ips.size());

        NetworkCheckResponse check = client.base().checkNetwork("all", "DEFAULT");
        assertEquals(1, check.getDns().size());
        assertEquals("111.161.64.40", check.getDns().getFirst().ip());

        QuotaResponse quota = client.base().getOpenApiQuota("/cgi-bin/message/custom/send");
        assertEquals(500000L, quota.getQuota().dailyLimit());
        assertEquals(499880L, quota.getQuota().remain());

        RidInfoResponse ridInfo = client.base().getRidInfo("61725984-693e1939-31932049");
        assertEquals(35L, ridInfo.getRequest().costInMs());
    }

    @Test
    void testMenuAndMessageServices() {
        Menu menu = new Menu()
                .addButton(MenuButton.ofClick("今日歌曲", "V1001_TODAY_MUSIC"))
                .addButton(MenuButton.ofParent("菜单", List.of(
                        MenuButton.ofView("搜索", "http://www.soso.com/")
                )));
        WeChatResponse createResp = client.menu().createMenu(menu);
        assertTrue(createResp.isSuccess());

        MenuGetResponse getResp = client.menu().getMenu();
        assertNotNull(getResp.getMenu());
        assertEquals("今日歌曲", getResp.getMenu().getButtons().getFirst().getName());

        WeChatResponse customResp = client.message().sendCustomMessage(
                CustomMessageRequest.ofText("oUser123", "您好，欢迎关注！").withKfAccount("kf01@test")
        );
        assertTrue(customResp.isSuccess());

        TemplateMessageRequest tplReq = new TemplateMessageRequest("oUser123", "TM_001")
                .addData("first", "恭喜您购买成功！")
                .addData("keyword1", "巧克力", "#173177");
        long msgId = client.message().sendTemplateMessage(tplReq);
        assertEquals(200228332L, msgId);

        SubscribeMessageRequest subReq = new SubscribeMessageRequest("oUser123", "PRI_TMPL_001")
                .addData("thing1", "订单已发货");
        assertTrue(client.message().sendSubscribeMessage(subReq).isSuccess());
    }

    @Test
    void testMaterialDraftPublishAndExtendedServices() {
        MediaUploadResponse uploadResp = client.material().uploadTempMedia(
                "image", "banner.jpg", new byte[]{10, 20, 30}, "image/jpeg"
        );
        assertEquals("MEDIA_ID_001", uploadResp.getMediaId());

        MediaDownloadResult downloadResult = client.material().getTempMedia("MEDIA_ID_001");
        assertEquals("test.jpg", downloadResult.fileName());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, downloadResult.data());

        MaterialCountResponse countResp = client.material().getMaterialCount();
        assertEquals(10, countResp.getImageCount());

        String draftMediaId = client.draft().addDraft(List.of(
                new DraftArticle("微信SDK发布指南", "<p>正文内容</p>", "THUMB_001")
        ));
        assertEquals("DRAFT_MEDIA_999", draftMediaId);

        String publishId = client.freePublish().submitPublish(draftMediaId);
        assertEquals("100000001", publishId);

        JsonNode userSummary = client.dataCube().getUserSummary("2026-11-01", "2026-11-01");
        assertEquals(15, userSummary.path("list").get(0).path("new_user").asInt());

        JsonNode ocrResult = client.intelligent().ocrIdCard("https://example.com/idcard.jpg");
        assertEquals("张三", ocrResult.path("name").asText());

        assertTrue(client.comment().openComment(10001L, 0).isSuccess());
        assertTrue(client.customService().addKfAccount("kf01@gh_test", "客服小张").isSuccess());
    }

    @Test
    void testUserAccountAndOAuthServices() {
        UserTag tag = client.user().createTag("广东");
        assertEquals(134, tag.id());
        assertEquals("广东", tag.name());

        UserInfo userInfo = client.user().getUserInfo("o6_bmjrPTlm6_2sgVt7hMZOPfL2M", "zh_CN");
        assertEquals(1, userInfo.getSubscribe());
        assertEquals("o6_bmasdasdsad6_2sgVt7hMZOPfL", userInfo.getUnionId());

        UserListResponse userList = client.user().getUserList(null);
        assertEquals(2, userList.getTotal());
        assertEquals(List.of("OPENID1", "OPENID2"), userList.getOpenIds());

        QrCodeTicket qrTicket = client.account().createTempQrCode("channel_01", 60);
        assertNotNull(qrTicket.getTicket());
        assertTrue(client.account().getQrCodeImageUrl(qrTicket.getTicket()).contains("/cgi-bin/showqrcode?ticket="));

        String shortKey = client.account().generateShortKey("https://example.com/very/long/payload", 3600);
        assertEquals("key_abc123", shortKey);
        ShortenFetchResponse fetched = client.account().fetchLongData(shortKey);
        assertEquals("https://example.com/very/long/payload", fetched.getLongData());

        String authUrl = client.oauth().buildAuthorizationUrl("https://example.com/callback", "snsapi_userinfo", "STATE_1", true);
        assertTrue(authUrl.startsWith("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + TEST_APP_ID));
        assertTrue(authUrl.endsWith("#wechat_redirect"));

        OAuthAccessToken oauthToken = client.oauth().getOAuthAccessToken("CODE_XYZ");
        assertEquals("OAUTH_TOKEN", oauthToken.getAccessToken());

        // 验证官网标准 JS-SDK 签名向量
        JsApiSignature jsSig = client.oauth().createJsApiSignature(
                "http://mp.weixin.qq.com?params=value#hash_ignored",
                "Wm3WZYTPz0wzccnW",
                1414587457L
        );
        assertEquals("0f9de62fce790f9a083d5c99e95740ceb90c27ed", jsSig.signature());
    }

    @Test
    void testWebhookEncryptionDecryptionAndRouter() {
        WeChatCryptUtil cryptUtil = new WeChatCryptUtil(TEST_APP_ID, TEST_TOKEN, TEST_AES_KEY);
        String incomingPlainXml = """
                <xml>
                <ToUserName><![CDATA[gh_abcdef123456]]></ToUserName>
                <FromUserName><![CDATA[oUser_OpenId_001]]></FromUserName>
                <CreateTime>1700000100</CreateTime>
                <MsgType><![CDATA[text]]></MsgType>
                <Content><![CDATA[你好微信]]></Content>
                <MsgId>1234567890123456</MsgId>
                </xml>
                """.trim();

        String timestamp = "1700000100";
        String nonce = "nonce_8888";
        String encryptedRequestXml = cryptUtil.encryptXml(incomingPlainXml, timestamp, nonce);
        String cipherText = WeChatCryptUtil.extractEncryptTag(encryptedRequestXml);
        String msgSignature = WeChatSignatureUtil.generateSignature(TEST_TOKEN, timestamp, nonce, cipherText);

        WeChatMessageRouter router = client.newMessageRouter()
                .onMsgType("text", msg -> PassiveReplyBuilder.replyText(msg, "收到您的消息：" + msg.getContent()))
                .onEvent("subscribe", msg -> PassiveReplyBuilder.replyText(msg, "感谢关注！"));

        // 1. 加密模式路由测试
        String encryptedResponseXml = router.handleWebhook(
                null, timestamp, nonce, "aes", msgSignature, encryptedRequestXml
        );
        assertTrue(encryptedResponseXml.contains("<Encrypt>"));
        String replyCipher = WeChatCryptUtil.extractEncryptTag(encryptedResponseXml);
        String decryptedReplyXml = cryptUtil.decrypt(replyCipher);

        WebhookMessage parsedReply = WebhookMessage.fromXml(decryptedReplyXml);
        assertEquals("oUser_OpenId_001", parsedReply.getToUserName());
        assertEquals("gh_abcdef123456", parsedReply.getFromUserName());
        assertEquals("收到您的消息：你好微信", parsedReply.getContent());

        // 2. 消息去重测试：同一 MsgId 再次推送应直接返回 "success"
        String duplicateReply = router.handleWebhook(
                null, timestamp, nonce, "aes", msgSignature, encryptedRequestXml
        );
        assertEquals("success", duplicateReply);
    }

    @Test
    void testApiErrorHandling() {
        registerJson("/cgi-bin/menu/delete", req ->
                "{\"errcode\":48001,\"errmsg\":\"api unauthorized rid: 654321\",\"rid\":\"654321\"}");

        WeChatApiException ex = assertThrows(WeChatApiException.class, () -> client.menu().deleteMenu());
        assertEquals(48001, ex.getErrCode());
        assertEquals("654321", ex.getRid());
        assertEquals("/cgi-bin/menu/delete", ex.getApiPath());
    }

    private void registerJson(String path, Function<RawHttpRequest, String> jsonGenerator) {
        mockRoutes.put(path, req -> {
            byte[] bytes = jsonGenerator.apply(req).getBytes(StandardCharsets.UTF_8);
            return new RawHttpResponse(200, Map.of("Content-Type", List.of("application/json; charset=UTF-8")), bytes);
        });
    }
}
