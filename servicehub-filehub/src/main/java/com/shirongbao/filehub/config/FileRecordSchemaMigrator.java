/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: file_record 表幂等迁移，为存量库补充 content_hash 列和唯一索引
 */
package com.shirongbao.filehub.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FileRecordSchemaMigrator implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    // 初始化表结构迁移器
    public FileRecordSchemaMigrator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 启动时检查并补充 content_hash 列和唯一索引
    @Override
    public void run(ApplicationArguments args) {
        Integer column = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'file_record' AND COLUMN_NAME = 'content_hash'",
                Integer.class);
        if (column != null && column == 0) {
            jdbc.execute("ALTER TABLE file_record ADD COLUMN content_hash VARCHAR(64) NULL AFTER content_type");
        }
        Integer index = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'file_record' AND INDEX_NAME = 'uk_content_hash'",
                Integer.class);
        if (index != null && index == 0) {
            jdbc.execute("ALTER TABLE file_record ADD UNIQUE INDEX uk_content_hash (content_hash)");
        }
    }
}
