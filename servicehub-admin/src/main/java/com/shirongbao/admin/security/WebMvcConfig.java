/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: Web 拦截器注册，保护管理端接口
 */
package com.shirongbao.admin.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AdminAuthInterceptor adminAuthInterceptor;

    // 初始化拦截器注册配置
    public WebMvcConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    // 注册管理端鉴权拦截器，放行登录、开放文件和开放短链接口
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/login", "/api/filehub/**", "/api/linkhub/**");
    }
}
