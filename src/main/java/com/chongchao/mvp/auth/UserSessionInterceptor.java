package com.chongchao.mvp.auth;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.domain.AppUser;
import com.chongchao.mvp.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserSessionInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final SessionRepository sessionRepository;

    public UserSessionInterceptor(TokenService tokenService, SessionRepository sessionRepository) {
        this.tokenService = tokenService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = BearerTokenExtractor.extract(request)
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED));
        String tokenHash = tokenService.hash(token);
        AppUser appUser = sessionRepository.findActiveUser(tokenHash)
                .orElseThrow(() -> new BusinessException("SESSION_EXPIRED", "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED));
        sessionRepository.touch(tokenHash);
        request.setAttribute(RequestAttributes.APP_USER, appUser);
        return true;
    }
}

