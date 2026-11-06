package com.wechat.sdk;

import com.wechat.sdk.api.AccountApiService;
import com.wechat.sdk.api.BaseApiService;
import com.wechat.sdk.api.CommentApiService;
import com.wechat.sdk.api.CustomServiceApiService;
import com.wechat.sdk.api.DataCubeApiService;
import com.wechat.sdk.api.DraftApiService;
import com.wechat.sdk.api.FreePublishApiService;
import com.wechat.sdk.api.IntelligentApiService;
import com.wechat.sdk.api.MaterialApiService;
import com.wechat.sdk.api.MenuApiService;
import com.wechat.sdk.api.MessageApiService;
import com.wechat.sdk.api.OAuthApiService;
import com.wechat.sdk.api.UserApiService;
import com.wechat.sdk.config.WeChatConfig;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.token.AccessTokenManager;
import com.wechat.sdk.token.DefaultAccessTokenManager;
import com.wechat.sdk.webhook.WeChatMessageRouter;

import java.util.Objects;

/**
 * 微信公众号（服务号）服务端 SDK 统一门面客户端 (Entrypoint)
 * <p>
 * 涵盖微信开放文档 (https://developers.weixin.qq.com/doc/service/api/) 全部核心服务端能力：
 * <ul>
 *   <li>基础支持与 OpenAPI 管理 ({@link #base()})</li>
 *   <li>自定义菜单与个性化菜单 ({@link #menu()})</li>
 *   <li>客服消息、模板消息、服务号订阅通知、高级群发 ({@link #message()})</li>
 *   <li>多客服账号、会话管理与聊天记录 ({@link #customService()})</li>
 *   <li>临时素材与永久素材管理 ({@link #material()})</li>
 *   <li>草稿箱管理 ({@link #draft()})</li>
 *   <li>发布能力管理 ({@link #freePublish()})</li>
 *   <li>图文消息留言管理 ({@link #comment()})</li>
 *   <li>用户管理：标签、用户信息、黑名单、账号迁移 ({@link #user()})</li>
 *   <li>账号管理：带参数二维码、短 Key 托管 ({@link #account()})</li>
 *   <li>网页开发：OAuth2 网页授权、JS-SDK 签名 ({@link #oauth()})</li>
 *   <li>数据统计 DataCube ({@link #dataCube()})</li>
 *   <li>智能接口：OCR、AI 语音识别、翻译、语义理解 ({@link #intelligent()})</li>
 *   <li>回调消息加解密与事件路由器 ({@link #newMessageRouter()})</li>
 * </ul>
 */
public class WeChatClient {

    private final WeChatConfig config;
    private final WeChatHttpClient httpClient;
    private final AccessTokenManager tokenManager;

    private final BaseApiService baseService;
    private final MenuApiService menuService;
    private final MessageApiService messageService;
    private final CustomServiceApiService customService;
    private final MaterialApiService materialService;
    private final DraftApiService draftService;
    private final FreePublishApiService freePublishService;
    private final CommentApiService commentService;
    private final UserApiService userService;
    private final AccountApiService accountService;
    private final OAuthApiService oauthService;
    private final DataCubeApiService dataCubeService;
    private final IntelligentApiService intelligentService;

    public WeChatClient(WeChatConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.httpClient = new WeChatHttpClient(config);
        this.tokenManager = new DefaultAccessTokenManager(config, this.httpClient);
        this.httpClient.setAccessTokenManager(this.tokenManager);

        this.baseService = new BaseApiService(this.httpClient, this.tokenManager);
        this.menuService = new MenuApiService(this.httpClient);
        this.messageService = new MessageApiService(this.httpClient);
        this.customService = new CustomServiceApiService(this.httpClient);
        this.materialService = new MaterialApiService(this.httpClient);
        this.draftService = new DraftApiService(this.httpClient);
        this.freePublishService = new FreePublishApiService(this.httpClient);
        this.commentService = new CommentApiService(this.httpClient);
        this.userService = new UserApiService(this.httpClient);
        this.accountService = new AccountApiService(this.httpClient);
        this.oauthService = new OAuthApiService(this.httpClient, this.tokenManager);
        this.dataCubeService = new DataCubeApiService(this.httpClient);
        this.intelligentService = new IntelligentApiService(this.httpClient);
    }

    public WeChatClient(WeChatConfig config, WeChatHttpClient httpClient, AccessTokenManager tokenManager) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.tokenManager = Objects.requireNonNull(tokenManager, "tokenManager must not be null");
        this.httpClient.setAccessTokenManager(this.tokenManager);

        this.baseService = new BaseApiService(this.httpClient, this.tokenManager);
        this.menuService = new MenuApiService(this.httpClient);
        this.messageService = new MessageApiService(this.httpClient);
        this.customService = new CustomServiceApiService(this.httpClient);
        this.materialService = new MaterialApiService(this.httpClient);
        this.draftService = new DraftApiService(this.httpClient);
        this.freePublishService = new FreePublishApiService(this.httpClient);
        this.commentService = new CommentApiService(this.httpClient);
        this.userService = new UserApiService(this.httpClient);
        this.accountService = new AccountApiService(this.httpClient);
        this.oauthService = new OAuthApiService(this.httpClient, this.tokenManager);
        this.dataCubeService = new DataCubeApiService(this.httpClient);
        this.intelligentService = new IntelligentApiService(this.httpClient);
    }

    public WeChatConfig config() {
        return config;
    }

    public WeChatHttpClient httpClient() {
        return httpClient;
    }

    public AccessTokenManager tokenManager() {
        return tokenManager;
    }

    public BaseApiService base() {
        return baseService;
    }

    public MenuApiService menu() {
        return menuService;
    }

    public MessageApiService message() {
        return messageService;
    }

    public CustomServiceApiService customService() {
        return customService;
    }

    public MaterialApiService material() {
        return materialService;
    }

    public DraftApiService draft() {
        return draftService;
    }

    public FreePublishApiService freePublish() {
        return freePublishService;
    }

    public CommentApiService comment() {
        return commentService;
    }

    public UserApiService user() {
        return userService;
    }

    public AccountApiService account() {
        return accountService;
    }

    public OAuthApiService oauth() {
        return oauthService;
    }

    public DataCubeApiService dataCube() {
        return dataCubeService;
    }

    public IntelligentApiService intelligent() {
        return intelligentService;
    }

    public WeChatMessageRouter newMessageRouter() {
        return new WeChatMessageRouter(config);
    }
}
