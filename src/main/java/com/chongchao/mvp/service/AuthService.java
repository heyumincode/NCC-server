package com.chongchao.mvp.service;

import com.chongchao.mvp.auth.TokenService;
import com.chongchao.mvp.config.AppProperties;
import com.chongchao.mvp.domain.AppUser;
import com.chongchao.mvp.dto.WechatLoginRequest;
import com.chongchao.mvp.dto.WechatLoginResponse;
import com.chongchao.mvp.repository.SessionRepository;
import com.chongchao.mvp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final WechatSessionClient wechatSessionClient;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;
    private final AppProperties appProperties;

    public AuthService(
            WechatSessionClient wechatSessionClient,
            UserRepository userRepository,
            SessionRepository sessionRepository,
            TokenService tokenService,
            AppProperties appProperties
    ) {
        this.wechatSessionClient = wechatSessionClient;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
        this.appProperties = appProperties;
    }

    @Transactional
    public WechatLoginResponse login(WechatLoginRequest request) {
        String openId = wechatSessionClient.exchangeOpenId(request.code());
        AppUser user = userRepository.upsert(
                openId,
                defaultString(request.nickname()),
                defaultString(request.avatarUrl())
        );
        String token = tokenService.newToken();
        Instant expiresAt = Instant.now().plus(appProperties.userSessionHours(), ChronoUnit.HOURS);
        sessionRepository.create(user.id(), tokenService.hash(token), expiresAt);
        return new WechatLoginResponse(token, expiresAt, user.id(), user.nickname(), user.avatarUrl());
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}

