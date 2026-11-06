package com.wechat.sdk.model.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义菜单按钮定义
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuButton {

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("key")
    private String key;

    @JsonProperty("url")
    private String url;

    @JsonProperty("media_id")
    private String mediaId;

    @JsonProperty("article_id")
    private String articleId;

    @JsonProperty("appid")
    private String appId;

    @JsonProperty("pagepath")
    private String pagePath;

    @JsonProperty("sub_button")
    private List<MenuButton> subButtons;

    public MenuButton() {
    }

    public static MenuButton ofClick(String name, String key) {
        MenuButton btn = new MenuButton();
        btn.setType("click");
        btn.setName(name);
        btn.setKey(key);
        return btn;
    }

    public static MenuButton ofView(String name, String url) {
        MenuButton btn = new MenuButton();
        btn.setType("view");
        btn.setName(name);
        btn.setUrl(url);
        return btn;
    }

    public static MenuButton ofMiniProgram(String name, String url, String appId, String pagePath) {
        MenuButton btn = new MenuButton();
        btn.setType("miniprogram");
        btn.setName(name);
        btn.setUrl(url);
        btn.setAppId(appId);
        btn.setPagePath(pagePath);
        return btn;
    }

    public static MenuButton ofParent(String name, List<MenuButton> subButtons) {
        MenuButton btn = new MenuButton();
        btn.setName(name);
        btn.setSubButtons(subButtons);
        return btn;
    }

    public MenuButton addSubButton(MenuButton subButton) {
        if (this.subButtons == null) {
            this.subButtons = new ArrayList<>();
        }
        this.subButtons.add(subButton);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public List<MenuButton> getSubButtons() {
        return subButtons;
    }

    public void setSubButtons(List<MenuButton> subButtons) {
        this.subButtons = subButtons;
    }
}
