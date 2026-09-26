package com.chongchao.mvp.auth;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.repository.StaffSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class StaffSessionInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final StaffSessionRepository sessionRepository;

    public StaffSessionInterceptor(TokenService tokenService, StaffSessionRepository sessionRepository) {
        this.tokenService = tokenService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = BearerTokenExtractor.extract(request)
                .orElseThrow(() -> new BusinessException("STAFF_UNAUTHORIZED", "请先进行工作人员登录", HttpStatus.UNAUTHORIZED));
        String tokenHash = tokenService.hash(token);
        String staffName = sessionRepository.findActiveStaffName(tokenHash)
                .orElseThrow(() -> new BusinessException("STAFF_SESSION_EXPIRED", "工作人员会话已过期", HttpStatus.UNAUTHORIZED));
        sessionRepository.touch(tokenHash);
        request.setAttribute(RequestAttributes.STAFF_NAME, staffName);
        return true;
    }
}

