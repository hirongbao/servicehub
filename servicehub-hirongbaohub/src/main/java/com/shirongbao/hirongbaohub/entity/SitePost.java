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
    private String categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<SitePostMedia> media;

    @TableField(exist = false)
    private List<SiteComment> comments;

    @TableField(exist = false)
    private Category category;

    // 动态分类返回对象
    public record Category(String id, String name) {
    }
}
