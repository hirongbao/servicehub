/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: Flyway 迁移策略配置，在迁移前自动执行 repair
 */
package com.shirongbao.admin.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    // 配置 Flyway 迁移策略，先 repair 后 migrate
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
