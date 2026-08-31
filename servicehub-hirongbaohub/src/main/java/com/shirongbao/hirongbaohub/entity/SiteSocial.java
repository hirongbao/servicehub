/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 个人网站社交名片实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_social")
public class SiteSocial {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String platform;
    private String iconName;
    private String url;
    private String qrCodeUrl;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
