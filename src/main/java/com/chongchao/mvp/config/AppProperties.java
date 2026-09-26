package com.chongchao.mvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chongchao")
public record AppProperties(
        String adminToken,
        String staffPasscode,
        long userSessionHours,
        long staffSessionHours,
        String ticketQrPrefix,
        Wechat wechat
) {
    public record Wechat(String appId, String appSecret, boolean mockEnabled) {
    }
}

