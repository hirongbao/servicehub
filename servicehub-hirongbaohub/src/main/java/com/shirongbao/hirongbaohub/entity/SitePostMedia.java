/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站动态媒体实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("site_post_media")
public class SitePostMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String mediaType;
    private String mediaUrl;
    private Integer sortOrder;
}
