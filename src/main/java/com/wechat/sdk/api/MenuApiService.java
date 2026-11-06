package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;
import com.wechat.sdk.model.WeChatResponse;
import com.wechat.sdk.model.menu.Menu;
import com.wechat.sdk.model.menu.MenuGetResponse;

import java.util.Map;
import java.util.Objects;

/**
 * 自定义菜单与个性化菜单管理接口服务
 * 对应官方文档：自定义菜单
 */
public class MenuApiService {

    private final WeChatHttpClient httpClient;

    public MenuApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * 创建自定义菜单 (/cgi-bin/menu/create)
     */
    public WeChatResponse createMenu(Menu menu) {
        Objects.requireNonNull(menu, "menu must not be null");
        return httpClient.postJson("/cgi-bin/menu/create", null, menu, WeChatResponse.class);
    }

    /**
     * 获取自定义菜单配置（包含默认菜单及个性化菜单）(/cgi-bin/menu/get)
     */
    public MenuGetResponse getMenu() {
        return httpClient.get("/cgi-bin/menu/get", null, MenuGetResponse.class);
    }

    /**
     * 查询自定义菜单配置（包括 API 设置或公众平台官网配置的菜单）(/cgi-bin/get_current_selfmenu_info)
     */
    public JsonNode getCurrentSelfMenuInfo() {
        return httpClient.get("/cgi-bin/get_current_selfmenu_info", null, JsonNode.class);
    }

    /**
     * 删除当前使用的自定义菜单（同时删除全部个性化菜单）(/cgi-bin/menu/delete)
     */
    public WeChatResponse deleteMenu() {
        return httpClient.get("/cgi-bin/menu/delete", null, WeChatResponse.class);
    }

    /**
     * 创建个性化菜单 (/cgi-bin/menu/addconditional)
     *
     * @return 个性化菜单 menuid
     */
    public String addConditionalMenu(Menu conditionalMenu) {
        Objects.requireNonNull(conditionalMenu, "conditionalMenu must not be null");
        JsonNode node = httpClient.postJson("/cgi-bin/menu/addconditional", null, conditionalMenu, JsonNode.class);
        return node.path("menuid").asText();
    }

    /**
     * 删除个性化菜单 (/cgi-bin/menu/delconditional)
     */
    public WeChatResponse deleteConditionalMenu(String menuId) {
        Objects.requireNonNull(menuId, "menuId must not be null");
        return httpClient.postJson("/cgi-bin/menu/delconditional", null, Map.of("menuid", menuId), WeChatResponse.class);
    }

    /**
     * 测试个性化菜单匹配结果 (/cgi-bin/menu/trymatch)
     *
     * @param userId 可以是粉丝的 OpenID，也可以是粉丝的微信号
     */
    public Menu tryMatchMenu(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        MenuGetResponse resp = httpClient.postJson("/cgi-bin/menu/trymatch", null, Map.of("user_id", userId), MenuGetResponse.class);
        return resp.getMenu();
    }
}
