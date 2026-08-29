/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: Web 拦截器注册，保护管理端接口
 */
package com.shirongbao.admin.security;

import com.shirongbao.admin.log.RequestLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final RequestLogInterceptor requestLogInterceptor = new RequestLogInterceptor();

    // 初始化拦截器注册配置
    public WebMvcConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    // 注册请求日志和管理端鉴权拦截器，放行登录、健康检查、开放文件和开放短链接口（Swagger 文档路径不在 /api/** 内，无需放行）
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor).addPathPatterns("/**");
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/login", "/api/health", "/api/filehub/**", "/api/linkhub/**");
    }
}
