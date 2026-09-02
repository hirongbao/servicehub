/*
 * auth: hirongbao
 * create: 2026-09-02
 * desc: 个人网站访客持久化统计实体
 */
package com.shirongbao.hirongbaohub.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_visitor")
public class SiteVisitor {
    @TableId
    private String visitorKey;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private LocalDateTime lastVisitAt;
    private Integer visitCount;
}
