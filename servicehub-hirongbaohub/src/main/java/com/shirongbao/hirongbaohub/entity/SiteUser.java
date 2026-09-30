package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_user")
public class SiteUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountName;
    private String email;
    private String passwordHash;
    private String role;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
