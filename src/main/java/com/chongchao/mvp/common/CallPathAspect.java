package com.chongchao.mvp.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * 在 DEBUG 级别下,自动给 service / repository 每个 public 方法加进入/退出日志。
 * 不需要给每个类加 @Slf4j 也能看到完整调用链:
 *   svc#findPublished() -> repo#findPublished() -> repo#findPublished() <-0ms, [0] rows
 *
 * 注意: Spring AOP 只拦截通过代理的调用,同类内 this.foo() 不会被拦截。
 *       本项目 service / repo 之间都是注入调用,不会有这个问题。
 */
@Aspect
@Component
public class CallPathAspect {

    private static final Logger log = LoggerFactory.getLogger(CallPathAspect.class);

    @Pointcut("execution(public * com.chongchao.mvp.service..*.*(..))")
    void serviceMethods() {}

    @Pointcut("execution(public * com.chongchao.mvp.repository..*.*(..))")
    void repositoryMethods() {}

    @Around("serviceMethods() || repositoryMethods()")
    public Object trace(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        String kind = sig.getDeclaringTypeName().contains(".repository.") ? "repo" : "svc";
        String method = sig.getName();
        Object[] args = pjp.getArgs();

        long startNs = System.nanoTime();
        log.debug("→ {}#{} ({})", kind, method, summarizeArgs(args));
        try {
            Object result = pjp.proceed();
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            log.debug("← {}#{} → {} ({}ms)", kind, method, summarize(result), elapsedMs);
            return result;
        } catch (Throwable t) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            log.warn("✗ {}#{} threw {} ({}ms)", kind, method, t.getClass().getSimpleName(), elapsedMs);
            throw t;
        }
    }

    private static String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object a = args[i];
            if (a == null) {
                sb.append("null");
            } else if (a instanceof Object[] arr) {
                sb.append(Arrays.toString(arr));
            } else {
                sb.append(a);
            }
        }
        return sb.toString();
    }

    private static String summarize(Object result) {
        if (result == null) return "null";
        if (result instanceof Optional<?> o) {
            return o.isPresent() ? "Optional[+]" : "Optional.empty";
        }
        if (result instanceof Collection<?> c) {
            return c.getClass().getSimpleName() + "[" + c.size() + "]";
        }
        if (result instanceof Long || result instanceof Integer
                || result instanceof Boolean || result instanceof String) {
            return result.toString();
        }
        return result.getClass().getSimpleName();
    }
}