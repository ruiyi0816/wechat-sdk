package com.wechat.sdk.http;

import java.util.Arrays;
import java.util.Objects;

/**
 * 多媒体素材下载结果封装（兼容二进制流、视频JSON返回等场景）
 */
public record MediaDownloadResult(
        byte[] data,
        String contentType,
        String fileName,
        String videoTitle,
        String videoDescription,
        String videoDownUrl
) {
    public boolean isVideoResource() {
        return videoDownUrl != null && !videoDownUrl.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaDownloadResult that)) return false;
        return Arrays.equals(data, that.data)
                && Objects.equals(contentType, that.contentType)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(videoTitle, that.videoTitle)
                && Objects.equals(videoDescription, that.videoDescription)
                && Objects.equals(videoDownUrl, that.videoDownUrl);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(contentType, fileName, videoTitle, videoDescription, videoDownUrl);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "MediaDownloadResult{" +
                "dataLength=" + (data != null ? data.length : 0) +
                ", contentType='" + contentType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", videoDownUrl='" + videoDownUrl + '\'' +
                '}';
    }
}
