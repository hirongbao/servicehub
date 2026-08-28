/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 服务 Token 使用日志实体
 */
package com.shirongbao.authhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_usage_log")
public class TokenUsageLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tokenId;
    private String hub;
    private String action;
    private LocalDateTime createdAt;
}
