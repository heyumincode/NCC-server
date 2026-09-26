package com.chongchao.mvp.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 入口过滤器：
 * - 注入请求 ID 到 MDC (便于全链路 grep)
 * - 优先读 X-Request-Id header,缺失则生成 8 字符短 ID
 * - 把 ID 写回响应头,客户端出错时可让用户提供
 * - 记录每个请求的方法/路径/查询串/UA/状态/耗时
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String inbound = req.getHeader(HEADER_REQUEST_ID);
        String requestId = (inbound != null && !inbound.isBlank() && inbound.length() <= 64)
                ? inbound
                : UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_REQUEST_ID, requestId);
        res.setHeader(HEADER_REQUEST_ID, requestId);

        long startNs = System.nanoTime();
        Throwable thrown = null;
        try {
            chain.doFilter(req, res);
        } catch (Throwable t) {
            thrown = t;
            throw t;
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            int status = res.getStatus();
            String method = req.getMethod();
            String uri = req.getRequestURI();
            String qs = req.getQueryString() == null ? "-" : req.getQueryString();
            String ua = truncate(req.getHeader("User-Agent"), 50);

            if (thrown == null) {
                if (status >= 500) {
                    log.error("{} {} qs={} ua={} -> {} {}ms", method, uri, qs, ua, status, elapsedMs);
                } else if (status >= 400) {
                    log.warn("{} {} qs={} ua={} -> {} {}ms", method, uri, qs, ua, status, elapsedMs);
                } else {
                    log.info("{} {} qs={} ua={} -> {} {}ms", method, uri, qs, ua, status, elapsedMs);
                }
            } else {
                log.error("{} {} qs={} ua={} -> uncaught {} {}ms",
                        method, uri, qs, ua, thrown.getClass().getSimpleName(), elapsedMs, thrown);
            }
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "-";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}