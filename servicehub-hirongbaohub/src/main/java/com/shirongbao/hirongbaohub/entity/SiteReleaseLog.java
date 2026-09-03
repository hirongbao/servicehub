/*
 * auth: hirongbao
 * create: 2026-09-03
 * desc: 个人网站更新日志实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("site_release_log")
public class SiteReleaseLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String version;
    private String summary;
    private String content;
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
