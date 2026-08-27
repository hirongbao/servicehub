package com.shirongbao.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.shirongbao")
public class ServiceHubApplication {
    public static void main(String[] args) { SpringApplication.run(ServiceHubApplication.class, args); }
}
