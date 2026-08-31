/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站站点资料实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_profile")
public class SiteProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String handle;
    private String bio;
    private String avatarUrl;
    private Integer statPosts;
    private Integer statFollowers;
    private Integer statFollowing;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
