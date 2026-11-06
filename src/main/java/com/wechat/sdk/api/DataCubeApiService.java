package com.wechat.sdk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.wechat.sdk.http.WeChatHttpClient;

import java.util.Map;
import java.util.Objects;

/**
 * 数据统计（DataCube）接口服务
 * 涵盖：用户分析、图文分析、消息分析、接口分析
 * 对应官方文档：数据统计
 */
public class DataCubeApiService {

    private final WeChatHttpClient httpClient;

    public DataCubeApiService(WeChatHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    // ==================== 1. 用户分析 ====================

    /**
     * 获取用户增减数据 (/datacube/getusersummary)，最大时间跨度 7 天
     *
     * @param beginDate 开始日期，格式 yyyy-MM-dd
     * @param endDate   结束日期，格式 yyyy-MM-dd
     */
    public JsonNode getUserSummary(String beginDate, String endDate) {
        return postDateRange("/datacube/getusersummary", beginDate, endDate);
    }

    /**
     * 获取累计用户数据 (/datacube/getusercumulate)，最大时间跨度 7 天
     */
    public JsonNode getUserCumulate(String beginDate, String endDate) {
        return postDateRange("/datacube/getusercumulate", beginDate, endDate);
    }

    // ==================== 2. 图文分析 ====================

    /**
     * 获取图文群发每日数据 (/datacube/getarticlesummary)，最大时间跨度 1 天
     */
    public JsonNode getArticleSummary(String beginDate, String endDate) {
        return postDateRange("/datacube/getarticlesummary", beginDate, endDate);
    }

    /**
     * 获取图文群发总数据 (/datacube/getarticletotal)，最大时间跨度 1 天
     */
    public JsonNode getArticleTotal(String beginDate, String endDate) {
        return postDateRange("/datacube/getarticletotal", beginDate, endDate);
    }

    /**
     * 获取图文统计数据 (/datacube/getuserread)，最大时间跨度 3 天
     */
    public JsonNode getUserRead(String beginDate, String endDate) {
        return postDateRange("/datacube/getuserread", beginDate, endDate);
    }

    /**
     * 获取图文统计分时数据 (/datacube/getuserreadhour)，最大时间跨度 1 天
     */
    public JsonNode getUserReadHour(String beginDate, String endDate) {
        return postDateRange("/datacube/getuserreadhour", beginDate, endDate);
    }

    /**
     * 获取图文分享转发数据 (/datacube/getusershare)，最大时间跨度 7 天
     */
    public JsonNode getUserShare(String beginDate, String endDate) {
        return postDateRange("/datacube/getusershare", beginDate, endDate);
    }

    /**
     * 获取图文分享转发分时数据 (/datacube/getusersharehour)，最大时间跨度 1 天
     */
    public JsonNode getUserShareHour(String beginDate, String endDate) {
        return postDateRange("/datacube/getusersharehour", beginDate, endDate);
    }

    // ==================== 3. 消息分析 ====================

    /**
     * 获取消息发送概况数据 (/datacube/getupstreammsg)，最大时间跨度 7 天
     */
    public JsonNode getUpstreamMsg(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsg", beginDate, endDate);
    }

    /**
     * 获取消息分送分时数据 (/datacube/getupstreammsghour)，最大时间跨度 1 天
     */
    public JsonNode getUpstreamMsgHour(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsghour", beginDate, endDate);
    }

    /**
     * 获取消息发送周数据 (/datacube/getupstreammsgweek)，最大时间跨度 30 天
     */
    public JsonNode getUpstreamMsgWeek(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsgweek", beginDate, endDate);
    }

    /**
     * 获取消息发送月数据 (/datacube/getupstreammsgmonth)，最大时间跨度 30 天
     */
    public JsonNode getUpstreamMsgMonth(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsgmonth", beginDate, endDate);
    }

    /**
     * 获取消息发送分布数据 (/datacube/getupstreammsgdist)，最大时间跨度 15 天
     */
    public JsonNode getUpstreamMsgDist(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsgdist", beginDate, endDate);
    }

    /**
     * 获取消息发送分布周数据 (/datacube/getupstreammsgdistweek)，最大时间跨度 30 天
     */
    public JsonNode getUpstreamMsgDistWeek(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsgdistweek", beginDate, endDate);
    }

    /**
     * 获取消息发送分布月数据 (/datacube/getupstreammsgdistmonth)，最大时间跨度 30 天
     */
    public JsonNode getUpstreamMsgDistMonth(String beginDate, String endDate) {
        return postDateRange("/datacube/getupstreammsgdistmonth", beginDate, endDate);
    }

    // ==================== 4. 接口分析 ====================

    /**
     * 获取接口分析数据 (/datacube/getinterfacesummary)，最大时间跨度 30 天
     */
    public JsonNode getInterfaceSummary(String beginDate, String endDate) {
        return postDateRange("/datacube/getinterfacesummary", beginDate, endDate);
    }

    /**
     * 获取接口分析分时数据 (/datacube/getinterfacesummaryhour)，最大时间跨度 1 天
     */
    public JsonNode getInterfaceSummaryHour(String beginDate, String endDate) {
        return postDateRange("/datacube/getinterfacesummaryhour", beginDate, endDate);
    }

    private JsonNode postDateRange(String path, String beginDate, String endDate) {
        Map<String, String> body = Map.of(
                "begin_date", Objects.requireNonNull(beginDate, "beginDate must not be null"),
                "end_date", Objects.requireNonNull(endDate, "endDate must not be null")
        );
        return httpClient.postJson(path, null, body, JsonNode.class);
    }
}
