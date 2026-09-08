/*
 * auth: hirongbao
 * create: 2026-09-08
 * desc: OpenAPI/Swagger 接口文档配置
 */
package com.shirongbao.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceHub API 文档")
                        .version("1.0.0")
                        .description("ServiceHub 对外开放的接口调用文档，支持媒体库、短链等模块。")
                        .contact(new Contact().name("hirongbao").url("https://hirongbao.com")))
                .components(new Components()
                        .addSecuritySchemes("X-Service-Token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Service-Token")
                                .description("使用 X-Service-Token Header 鉴权"))
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("使用标准 Authorization Header (Bearer) 鉴权")));
    }
}
