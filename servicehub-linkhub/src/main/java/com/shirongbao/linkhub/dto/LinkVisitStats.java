/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链访问统计聚合结果
 */
package com.shirongbao.linkhub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkVisitStats {
    private Long linkId;
    private Long visitCount;
    private LocalDateTime lastVisitAt;
}
