/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: ServiceHub 管理后台应用启动类
 */
package com.shirongbao.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shirongbao")
@MapperScan("com.shirongbao.**.mapper")
public class ServiceHubApplication {
    // 启动 ServiceHub 管理后台应用
    public static void main(String[] args) { SpringApplication.run(ServiceHubApplication.class, args); }
}
