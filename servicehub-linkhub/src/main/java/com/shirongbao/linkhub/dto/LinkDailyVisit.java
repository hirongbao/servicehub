/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链按天访问统计
 */
package com.shirongbao.linkhub.dto;

import lombok.Data;

@Data
public class LinkDailyVisit {
    private String visitDate;
    private Long visits;
}
