package com.wechat.sdk.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义菜单及个性化菜单配置实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Menu {

    @JsonProperty("button")
    private List<MenuButton> buttons = new ArrayList<>();

    @JsonProperty("matchrule")
    private MatchRule matchRule;

    @JsonProperty("menuid")
    private String menuId;

    public Menu() {
    }

    public Menu(List<MenuButton> buttons) {
        this.buttons = buttons;
    }

    public Menu addButton(MenuButton button) {
        if (this.buttons == null) {
            this.buttons = new ArrayList<>();
        }
        this.buttons.add(button);
        return this;
    }

    public List<MenuButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<MenuButton> buttons) {
        this.buttons = buttons;
    }

    public MatchRule getMatchRule() {
        return matchRule;
    }

    public void setMatchRule(MatchRule matchRule) {
        this.matchRule = matchRule;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    /**
     * 个性化菜单匹配规则
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MatchRule(
            @JsonProperty("tag_id") String tagId,
            @JsonProperty("client_platform_type") String clientPlatformType,
            @JsonProperty("language") String language
    ) {
    }
}
