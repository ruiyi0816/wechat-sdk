package com.wechat.sdk.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户标签实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserTag(
        @JsonProperty("id") Integer id,
        @JsonProperty("name") String name,
        @JsonProperty("count") Integer count
) {
}
