# WeChat Official Account (Service Account) Java SDK

基于 **JDK 21** 与 **Maven** 构建的微信公众号（服务号）服务端 Java SDK，完整覆盖微信开放文档（[服务号服务端 API](https://developers.weixin.qq.com/doc/service/api/)）核心能力。

## 特性概览

- **原生 JDK 21 支持**：基于 `java.net.http.HttpClient`、Java `record`、文本块、switch 模式匹配等现代特性构建，极简三方依赖（仅依赖 `jackson-databind` 与 `slf4j-api`）。全项目 JDK 21 特性审计与用法见 [JDK21-FEATURES.md](JDK21-FEATURES.md)。
- **AccessToken 与 Ticket 全生命周期管理**：
  - 内置线程安全的 `DefaultAccessTokenManager`，支持普通凭据（`/cgi-bin/token`）与稳定版凭据（`/cgi-bin/stable_token`）。
  - 支持预留过期缓冲窗口自动刷新，以及遇到 `40001` / `40014` / `42001` 错误码时自动刷新 Token 并无缝重试请求。
  - 提供 `AccessTokenManager` 接口，方便集群/分布式环境接入 Redis 等集中式存储。
- **全模块服务端 API 覆盖**：
  1. **基础与 OpenAPI 管理 (`client.base()`)**：AccessToken、稳定版 Token、微信服务器 IP 列表、回调 IP 列表、网络检测、Quota 清空与查询、`rid` 报错详情查询。
  2. **自定义菜单 (`client.menu()`)**：自定义菜单创建/查询/删除、个性化菜单创建/删除/匹配测试。
  3. **消息与通知 (`client.message()`)**：客服消息、客服输入状态、模板消息全流程、服务号订阅通知（`newtmpl` & `bizsend`）、高级群发（按标签/按 OpenID/预览/测速）。
  4. **多客服管理 (`client.customService()`)**：客服账号增删改、邀请绑定、头像上传、在线客服列表、会话控制、聊天记录拉取。
  5. **素材管理 (`client.material()`)**：临时素材上传与下载、高清语音获取、图文内图片上传、永久素材上传（含视频描述表单）/下载/删除/总数与分批列表。
  6. **草稿箱 (`client.draft()`)**：草稿新建、获取、修改、删除、计数、分页拉取及开关设置。
  7. **发布能力 (`client.freePublish()`)**：草稿正式发布、状态轮询、已发布文章获取、删除与列表查询。
  8. **图文留言管理 (`client.comment()`)**：打开/关闭评论、评论列表查询、精选/取消精选、删除评论、回复与删除回复。
  9. **用户管理 (`client.user()`)**：用户标签管理、批量打标/取消标签、用户基本信息（UnionID 机制）单条及批量拉取、备注设置、关注列表、黑名单管理、账号迁移 OpenID 转换。
  10. **账号管理 (`client.account()`)**：临时/永久带参数二维码生成、二维码图片 URL 构造与二进制下载、短 Key 托管生成与还原。
  11. **网页开发 (`client.oauth()`)**：OAuth2 授权链接构造、`code` 换取网页授权 Token、刷新 Token、拉取用户信息、JS-SDK `jsapi_ticket` 缓存及 `wx.config` 签名生成。
  12. **数据统计 (`client.dataCube()`)**：用户分析、图文分析、消息分析、接口分析全量报表接口。
  13. **智能接口 (`client.intelligent()`)**：身份证/银行卡/行驶证/驾驶证/营业执照/通用印刷体 OCR、AI 语音识别、微信翻译、语义理解。
- **回调消息加解密与事件路由器 (`client.newMessageRouter()`)**：
  - 完整实现微信官方 `WXBizMsgCrypt`（AES-256-CBC + 32 字节 PKCS#7 填充）消息加解密与 SHA-1 签名验证。
  - 内置 `MsgId` 与事件维度防重放/去重机制，支持链式规则路由及 `PassiveReplyBuilder` 被动回复 XML 构建。

---

## 环境要求

- **JDK**: 21 及以上
- **Maven**: 3.8+

## 快速开始

### 1. 初始化 `WeChatClient`

```java
import com.wechat.sdk.WeChatClient;
import com.wechat.sdk.config.WeChatConfig;

WeChatConfig config = WeChatConfig.builder()
        .appId("wx1234567890abcdef")
        .appSecret("your_app_secret")
        .token("your_webhook_token")
        .encodingAesKey("your_43_chars_encoding_aes_key")
        .useStableToken(true) // 可选：使用稳定版 access_token (/cgi-bin/stable_token)
        .autoRetryOnTokenExpired(true)
        .build();

WeChatClient client = new WeChatClient(config);
```

### 2. 发送模板消息与客服消息

```java
import com.wechat.sdk.model.message.CustomMessageRequest;
import com.wechat.sdk.model.message.TemplateMessageRequest;

// 发送文本客服消息
client.message().sendCustomMessage(
        CustomMessageRequest.ofText("oUserOpenId", "您好，请问有什么可以帮您？")
);

// 发送模板消息
TemplateMessageRequest tplRequest = new TemplateMessageRequest("oUserOpenId", "TEMPLATE_ID")
        .addData("first", "您的订单已发货")
        .addData("keyword1", "SF1234567890", "#173177")
        .addData("remark", "感谢您的惠顾！");
long msgId = client.message().sendTemplateMessage(tplRequest);
```

### 3. 处理微信回调通知与事件路由

```java
import com.wechat.sdk.webhook.PassiveReplyBuilder;
import com.wechat.sdk.webhook.WeChatMessageRouter;

WeChatMessageRouter router = client.newMessageRouter()
        .onMsgType("text", msg -> PassiveReplyBuilder.replyText(msg, "已收到：" + msg.getContent()))
        .onEvent("subscribe", msg -> PassiveReplyBuilder.replyText(msg, "欢迎关注本服务号！"));

// 在 Controller 中处理 POST 回调（自动识别明文或 AES 安全模式）
String responseXml = router.handleWebhook(
        signature, timestamp, nonce, encryptType, msgSignature, requestBodyXml
);
```

## 构建与测试

```bash
mvn clean test
```
