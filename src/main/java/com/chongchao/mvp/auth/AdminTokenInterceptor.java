package com.chongchao.mvp.auth;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.config.AppProperties;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AdminTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminTokenInterceptor.class);

    private final AppProperties appProperties;
    private final TokenService tokenService;
    private final Environment environment;

    public AdminTokenInterceptor(AppProperties appProperties, TokenService tokenService, Environment environment) {
        this.appProperties = appProperties;
        this.tokenService = tokenService;
        this.environment = environment;
    }

    /**
     * 启动时打印当前生效的 admin token 预览 + profile。
     * 避免“输错 token 一直 401 但不知道服务器期望什么”的扯皮。
     * token 只打前 4 后 4,中间省掉。
     */
    @PostConstruct
    void logStartupConfig() {
        String token = appProperties.adminToken();
        String profiles = Arrays.toString(environment.getActiveProfiles());
        if (token == null || token.isBlank()) {
            log.warn("[admin] admin-token NOT configured (profile={}); 所有 /api/v1/admin/* 会被 503 拒绝", profiles);
            return;
        }
        String masked = token.length() > 8
                ? token.substring(0, 4) + "…" + token.substring(token.length() - 4)
                : "***";
        log.info("[admin] admin-token configured: '{}' (length={}, profile={})", masked, token.length(), profiles);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String configuredToken = appProperties.adminToken();
        if (configuredToken == null || configuredToken.isBlank()) {
            throw new BusinessException("ADMIN_NOT_CONFIGURED", "管理端尚未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
        String suppliedToken = request.getHeader("X-Admin-Token");
        if (!tokenService.constantTimeEquals(configuredToken, suppliedToken)) {
            throw new BusinessException("ADMIN_UNAUTHORIZED", "管理令牌不正确", HttpStatus.UNAUTHORIZED);
        }
        return true;
    }
}

