package com.chongchao.mvp.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.chongchao.mvp.auth.TokenService;
import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WechatSessionClient {

    private final AppProperties appProperties;
    private final TokenService tokenService;
    private final RestClient restClient;

    public WechatSessionClient(
            AppProperties appProperties,
            TokenService tokenService,
            RestClient.Builder restClientBuilder
    ) {
        this.appProperties = appProperties;
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
    }

    public String exchangeOpenId(String code) {
        if (appProperties.wechat().mockEnabled()) {
            return "mock-" + tokenService.hash(code).substring(0, 32);
        }
        if (isBlank(appProperties.wechat().appId()) || isBlank(appProperties.wechat().appSecret())) {
            throw new BusinessException("WECHAT_NOT_CONFIGURED", "微信登录尚未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
        WechatSessionResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sns/jscode2session")
                        .queryParam("appid", appProperties.wechat().appId())
                        .queryParam("secret", appProperties.wechat().appSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(WechatSessionResponse.class);
        if (response == null || isBlank(response.openId())) {
            String detail = response == null ? "微信接口无响应" : response.errorMessage();
            throw new BusinessException("WECHAT_LOGIN_FAILED", "微信登录失败：" + detail, HttpStatus.BAD_GATEWAY);
        }
        return response.openId();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record WechatSessionResponse(
            @JsonProperty("openid") String openId,
            @JsonProperty("errcode") Integer errorCode,
            @JsonProperty("errmsg") String errorMessage
    ) {
    }
}

