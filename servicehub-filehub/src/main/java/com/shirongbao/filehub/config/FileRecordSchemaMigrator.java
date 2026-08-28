/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: file_record 表幂等迁移，补 content_hash 列、唯一索引并回填存量记录哈希
 */
package com.shirongbao.filehub.config;

import com.shirongbao.filehub.util.ContentHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Component
public class FileRecordSchemaMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(FileRecordSchemaMigrator.class);
    private final JdbcTemplate jdbc;

    // 初始化表结构迁移器
    public FileRecordSchemaMigrator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 启动时补充缺失的列、索引，并回填存量记录哈希
    @Override
    public void run(ApplicationArguments args) {
        ensureColumn();
        ensureIndex();
        backfillContentHash();
    }

    // 检查并补充 content_hash 列
    private void ensureColumn() {
        Integer column = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'file_record' AND COLUMN_NAME = 'content_hash'",
                Integer.class);
        if (column != null && column == 0) {
            jdbc.execute("ALTER TABLE file_record ADD COLUMN content_hash VARCHAR(64) NULL AFTER content_type");
        }
    }

    // 检查并补充 content_hash 唯一索引
    private void ensureIndex() {
        Integer index = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'file_record' AND INDEX_NAME = 'uk_content_hash'",
                Integer.class);
        if (index != null && index == 0) {
            jdbc.execute("ALTER TABLE file_record ADD UNIQUE INDEX uk_content_hash (content_hash)");
        }
    }

    // 为存量记录从 COS 拉取内容并回填哈希，内容重复的冗余记录直接清理
    private void backfillContentHash() {
        List<Long> ids = jdbc.queryForList(
                "SELECT id FROM file_record WHERE content_hash IS NULL", Long.class);
        for (Long id : ids) {
            String url = jdbc.queryForObject("SELECT file_url FROM file_record WHERE id = ?", String.class, id);
            try (InputStream in = URI.create(url).toURL().openStream()) {
                String hash = ContentHash.of(in);
                jdbc.update("UPDATE file_record SET content_hash = ? WHERE id = ?", hash, id);
            } catch (DuplicateKeyException e) {
                jdbc.update("DELETE FROM file_record WHERE id = ?", id);
                log.warn("文件记录 {} 与其他记录内容重复，已删除冗余记录", id);
            } catch (Exception e) {
                log.warn("回填文件记录 {} 的内容哈希失败：{}", id, e.getMessage());
            }
        }
    }
}
