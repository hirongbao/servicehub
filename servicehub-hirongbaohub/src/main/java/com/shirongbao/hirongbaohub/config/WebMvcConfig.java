package com.shirongbao.hirongbaohub.config;

import com.shirongbao.hirongbaohub.security.UserAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("hirongbaohubWebMvcConfig")
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserAuthInterceptor authInterceptor;

    public WebMvcConfig(UserAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/user/info")
                .addPathPatterns("/api/guestbook/**")
                .excludePathPatterns("/api/guestbook/list")
                .addPathPatterns("/api/posts/ugc/**"); // For posting UGC
    }
}
