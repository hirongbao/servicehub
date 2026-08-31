/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: OpenAPI 文档配置
 */
package com.shirongbao.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // 声明 OpenAPI 文档信息与管理端、开放接口的安全方案
    @Bean
    public OpenAPI serviceHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceHub API")
                        .description("个人项目基础服务平台：管理端、开放文件接口（FILEHUB）、开放短链接口（LINKHUB）与个人网站公开接口（HIRONGBAOHUB）。"
                                + "管理端接口使用登录时返回的 Bearer 凭证，开放接口使用 X-Service-Token 请求头携带对应类型的 Token，个人网站公开接口无需凭证。")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("AdminToken", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer")
                                .description("管理员登录凭证"))
                        .addSecuritySchemes("ServiceToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Service-Token")
                                .description("服务 Token（FILEHUB / LINKHUB 类型）")));
    }
}
