# JDK 21 特性应用分析

本文档是对 `wechat-sdk` 全项目（53 个主代码文件 + 1 个测试文件，约 6.3k 行）使用 JDK 21
新特性可行性的审计结果，记录本次已应用的改造、项目已在使用中的现代 Java 特性，以及
经评估后**不适用**或**暂不采用**的特性及原因。

## 一、本次应用（增量改造）

| 特性 | JEP / API | 位置 | 改造说明 |
| --- | --- | --- | --- |
| 文本块 Text Blocks | JEP 378 | `crypto/WeChatCryptUtil#encryptXml` | 安全模式 XML 报文由 `+` 拼接改为文本块 + `formatted(...)`，报文格式与输出字节保持不变 |
| switch 模式匹配（含 `case null`） | JEP 441 | `util/JsonUtils#toJson` | `null` / `String` / 其他对象三路分支用 `switch` 模式匹配替代连续 `if` |
| switch 表达式 | JEP 361 | `exception/WeChatApiException#isAccessTokenInvalid` | 多个错误码合并为 `case 40001, 40014, 42001 -> true`，消除 `||` 链 |
| record 值对象 | JEP 395 | `webhook/WebhookMessage` | 单字段不可变载体改为 `record`，保留原构造器、`getRawFields()` 等全部公共方法，新增值语义 `equals/hashCode` 与 `rawFields()` 访问器 |
| 行为回归测试 | — | `test/WeChatSdkTest#testJdk21LanguageFeatureBehaviors` | 新增用例覆盖上述改造后的行为不变性 |

附带清理：`crypto/WeChatCryptUtil#pkcs7Encode` 删除 `if (amountToPad == 0)` 分支——`32 - (count % 32)`
的取值恒在 1..32，该分支不可达，删除后填充结果不变。

## 二、项目已在使用的现代 Java 特性（保留）

- **record**：`AccessToken`、`JsApiSignature`、`MediaDownloadResult`、`HttpTransport.RawHttpRequest/RawHttpResponse`、
  `WeChatMessageRouter.RouteRule`、`PassiveReplyBuilder.ArticleItem` 及各响应 DTO 的嵌套类型等 20+ 处。
- **文本块**：`PassiveReplyBuilder` 的全部被动回复 XML 模板。
- **instanceof 模式匹配**（JEP 394）：`MediaDownloadResult#equals` 等（`JsonUtils#toJson` 本次已升级为模式 switch）。
- **Sequenced Collections**（JEP 431）：`HttpTransport.RawHttpResponse#firstHeader` 使用 `List#getFirst()`。
- **`java.net.http.HttpClient`**：HTTP/2、连接/请求超时、重定向策略。
- **`HexFormat`**（JDK 17）：`WeChatSignatureUtil#sha1Hex` 的十六进制摘要。
- **Stream API / `toList()`**：`UserApiService#batchGetUserInfo`。
- **`var` 局部变量类型推断**：`BaseApiService`、`WeChatCryptUtil` 等。

## 三、已评估但不适用 / 未采用

| 特性 | 结论与理由 |
| --- | --- |
| 记录模式 Record Patterns（JEP 440） | 项目中的 record 均为扁平数据传输对象，没有需要解构的嵌套 record 树；在 `MediaDownloadResult#equals` 中使用记录模式反而比现有 `instanceof` 模式更冗长，故不采用。 |
| 密封类/接口 Sealed（JEP 409） | `HttpTransport`（用户可实现的自定义传输层）、`AccessTokenManager`（用户可接入 Redis 等集中式存储）、`WeChatException` 均为公共扩展点，封闭会破坏使用方扩展能力；响应模型采用继承 + Jackson 反序列化，密封无实际收益。 |
| 虚拟线程 Virtual Threads（JEP 444） | SDK 为同步 HTTP API，调用线程直接阻塞在 `HttpClient.send`，内部没有任务提交/并发扇出点；为 `HttpClient` 配置虚拟线程执行器对同步调用没有实际收益。使用方按需在自身虚拟线程中调用本 SDK 即可。 |
| 字符串模板 String Templates（JEP 430） | JDK 21 预览特性，需 `--enable-preview` 编译运行，生产库不启用预览特性，故不采用。 |
| 未命名模式/变量 Unnamed Patterns & Variables（JEP 443） | 同上，JDK 21 预览特性，不采用。 |
| 未命名类 Unnamed Classes（JEP 445） | 同上，JDK 21 预览特性，不采用。 |
| 序列化集合扩展用法 | 已在 `firstHeader` 使用 `getFirst()`；其余集合操作没有天然适合 `getLast()`/`reversed()` 的场景，不强行套用。 |
| `String.formatted()` 改写单行拼接 | 曾尝试将 `OAuthApiService#createJsApiSignature` 与 `WeChatResponse#toString` 的单行拼接改为 `formatted(...)`，评估后**回退**：`formatted` 等价于使用 JVM 默认 Locale 的 `String.format`，`%d` 在 ar-EG/fa-IR 等区域会输出本地化数字，会让 JS-SDK 签名原文随环境变化（属行为回归），而原拼接与 Locale 无关。文本块内部的 `formatted(...)` 仅使用 `%s` 字符串参数，不受 Locale 影响，故保留。 |
| `Math.clamp`（Java 21 API） | 全项目唯一候选为 PKCS#7 补齐处，但 `32 - count % 32` 的取值恒在 1..32，`clamp` 是 no-op，不能构成真实应用；其余截断逻辑（如 `Math.max(0, maxRetries)`、token 有效期下限）是单边截断，`Math.clamp` 语义不匹配。该处实际问题是不可达的防御分支，已直接删除（见第一节）。 |
| 分代 ZGC（JEP 439）等 JVM 特性 | 属于运行时/部署参数（如 `-XX:+UseZGC -XX:+ZGenerational`），与源码无关，不在本次代码改造范围。 |

## 四、未采用的破坏性改造（需产品决策）

1. **将响应/请求模型类改为 record**：15 个响应类继承 `WeChatResponse`，而 record 不能继承类，同时它们依赖无参构造器 + setter 供 Jackson 反序列化、对外暴露 `getXxx()/setXxx()`；另有 7 个可变请求/菜单类（`CustomMessageRequest`、`TemplateMessageRequest`、`MassMessageRequest`、`SubscribeMessageRequest`、`DraftArticle`、`Menu`、`MenuButton`）依赖无参构造 + setter + 链式工厂。合计 23 个非 record 模型类（含 `WeChatResponse` 基类）的改造会破坏继承关系、构造器、setter 与 getter 命名，属于公共 API 破坏性变更，需按大版本升级处理。
2. **将 `MenuButton` 的 `type` 字符串改为 sealed interface 类型层级**：会改变 JSON 结构与公共
   API 契约，属于模型层重设计，需单独需求驱动。

以上两项在本次任务中只做分析记录，未实施。

## 五、验证方式

```bash
# 需要 JDK 21 与 Maven 3.8+
mvn clean test
```

验证结果（JDK 21.0.13 / Temurin + Maven 3.9.8）：

- 基线（master `7041818`）：`mvn clean test` — 7 个用例全部通过，证明基线本身在 JDK 21 上可构建。
- 本次改造后：`mvn clean test` — 10 个用例全部通过（7 个原有回归用例 + 3 个新增用例：
  JDK 21 特性行为、JS-SDK 签名 Locale 无关性、PKCS#7 整块填充边界）。
- 默认 Locale 与 `-Duser.language=ar -Duser.country=EG`（非 ASCII 数字区域）下测试均通过，
  确认签名/日志输出不依赖区域设置。
- 编译：`maven-compiler-plugin` 以 `--release 21` 编译通过，无 preview 特性、无编译告警。
- 等价性核对：`encryptXml` 新旧实现输出逐字节一致；`pkcs7Encode` 仅删除不可达分支，独立验证对
  count 0..100000 的新旧实现逐一比对一致（分支不可达的静态依据：`32 - count % 32` 恒在 1..32）。
- 重点回归点：`encryptXml` 输出格式（安全模式 XML 报文字节格式不变）、PKCS#7 填充边界
  （count % 32 == 0 时仍补满 32 字节，新增边界用例覆盖）、`toJson` 的 null/字符串/对象三态、
  access_token 失效错误码识别。
