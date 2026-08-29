/*
 * auth: hirongbao
 * create: 2026-08-29
 * desc: 短链访问来源或设备维度统计
 */
package com.shirongbao.linkhub.dto;

import lombok.Data;

@Data
public class LinkLabelVisit {
    private String label;
    private Long visits;
}
