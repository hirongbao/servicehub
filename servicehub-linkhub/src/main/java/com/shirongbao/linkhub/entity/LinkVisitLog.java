/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链访问日志实体
 */
package com.shirongbao.linkhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("link_visit_log")
public class LinkVisitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long linkId;
    private LocalDateTime createdAt;
}
