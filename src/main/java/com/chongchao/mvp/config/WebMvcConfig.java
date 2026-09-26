package com.chongchao.mvp.config;

import com.chongchao.mvp.auth.AdminTokenInterceptor;
import com.chongchao.mvp.auth.StaffSessionInterceptor;
import com.chongchao.mvp.auth.UserSessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserSessionInterceptor userSessionInterceptor;
    private final StaffSessionInterceptor staffSessionInterceptor;
    private final AdminTokenInterceptor adminTokenInterceptor;

    public WebMvcConfig(
            UserSessionInterceptor userSessionInterceptor,
            StaffSessionInterceptor staffSessionInterceptor,
            AdminTokenInterceptor adminTokenInterceptor
    ) {
        this.userSessionInterceptor = userSessionInterceptor;
        this.staffSessionInterceptor = staffSessionInterceptor;
        this.adminTokenInterceptor = adminTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSessionInterceptor)
                .addPathPatterns("/api/v1/tickets/**");
        registry.addInterceptor(staffSessionInterceptor)
                .addPathPatterns("/api/v1/staff/**")
                .excludePathPatterns("/api/v1/staff/login");
        registry.addInterceptor(adminTokenInterceptor)
                .addPathPatterns("/api/v1/admin/**");
    }
}

