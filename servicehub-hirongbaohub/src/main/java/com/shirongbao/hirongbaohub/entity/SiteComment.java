/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站访客评论实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_comment")
public class SiteComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String author;
    private String content;
    private LocalDateTime createdAt;
}
