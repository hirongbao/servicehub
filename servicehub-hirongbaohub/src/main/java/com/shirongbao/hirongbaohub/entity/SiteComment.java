/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站访客评论实体
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
@TableName("site_comment")
public class SiteComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long parentId;
    private String author;
    private String ipAddress;
    private String replyToAuthor;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private List<SiteComment> children;
}
