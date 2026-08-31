/*
 * auth: hirongbao
 * create: 2026-08-31
 * desc: 站点资料响应（含社交名片与统计数字）
 */
package com.shirongbao.hirongbaohub.dto;

import java.util.List;

public record ProfileResponse(String name, String handle, String bio, String avatarUrl,
                              List<SocialItem> socials, Stats stats) {

    // 社交名片：qrCodeUrl 用于二维码弹窗展示，url 用于外链跳转，二者至少有一个
    public record SocialItem(String platform, String iconName, String url, String qrCodeUrl) {
    }

    // 统计数字：followers 返回原始数值，由前端自行格式化（如 12400 显示为 12.4k）
    public record Stats(long posts, long followers, long following) {
    }
}
