/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: ServiceHub 业务 Token 数据实体
 */
package com.shirongbao.authhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_token")
public class ServiceToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tokenName;
    private String tokenValue;
    private String tokenType;
    private Integer maxUses;
    private Integer status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Long usageCount;
    @TableField(exist = false)
    private LocalDateTime lastUsedAt;
}
