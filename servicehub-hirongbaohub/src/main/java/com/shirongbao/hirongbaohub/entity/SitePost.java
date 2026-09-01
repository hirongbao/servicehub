/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站动态实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("site_post")
public class SitePost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<SitePostMedia> media;
}
