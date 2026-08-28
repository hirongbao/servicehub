/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链数据实体
 */
package com.shirongbao.linkhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("short_link")
public class ShortLink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String targetUrl;
    private String remark;
    private Integer status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Long visitCount;
    @TableField(exist = false)
    private LocalDateTime lastVisitAt;
}
