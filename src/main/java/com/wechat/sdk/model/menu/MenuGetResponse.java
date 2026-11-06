package com.wechat.sdk.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.sdk.model.WeChatResponse;

import java.util.List;

/**
 * 查询自定义菜单响应 (/cgi-bin/menu/get)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuGetResponse extends WeChatResponse {

    @JsonProperty("menu")
    private Menu menu;

    @JsonProperty("conditionalmenu")
    private List<Menu> conditionalMenus;

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public List<Menu> getConditionalMenus() {
        return conditionalMenus;
    }

    public void setConditionalMenus(List<Menu> conditionalMenus) {
        this.conditionalMenus = conditionalMenus;
    }
}
