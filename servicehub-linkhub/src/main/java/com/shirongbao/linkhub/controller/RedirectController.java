/*
 * auth: hirongbao
 * create: 2026-08-28
 * desc: 短链跳转入口
 */
package com.shirongbao.linkhub.controller;

import com.shirongbao.common.util.IpUtils;
import com.shirongbao.linkhub.entity.ShortLink;
import com.shirongbao.linkhub.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@RestController
public class RedirectController {
    private static final String NOT_FOUND_PAGE = """
            <!DOCTYPE html>
            <html lang="zh-CN"><head><meta charset="UTF-8"><title>链接不存在</title>
            <style>body{display:grid;place-items:center;min-height:100vh;margin:0;font-family:system-ui,sans-serif;background:#fafafa;color:#1a1b1f}
            .box{text-align:center}.box h1{font-size:44px;margin:0 0 8px;color:#a1a5ae}.box p{color:#6f737d;font-size:14px}</style></head>
            <body><div class="box"><h1>404</h1><p>链接不存在或已失效</p></div></body></html>
            """;
    private final ShortLinkService service;

    // 初始化短链跳转控制器
    public RedirectController(ShortLinkService service) {
        this.service = service;
    }

    // 处理短链跳转，记录访问并 302 到目标地址
    @GetMapping("/s/{code}")
    public ResponseEntity<String> redirect(@PathVariable String code, HttpServletRequest request) {
        ShortLink link = service.resolve(code);
        if (link == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(NOT_FOUND_PAGE);
        }
        service.recordVisit(link.getId(), request.getHeader("Referer"), request.getHeader("User-Agent"), IpUtils.getClientIp(request));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                .location(java.net.URI.create(link.getTargetUrl()))
                .build();
    }

    private final RestTemplate restTemplate = new RestTemplate();

    // 专为 GitHub README 等不支持 302 跳转的严格代理系统设计的图片透传接口
    @GetMapping({"/s/img/{code}", "/s/img/{code}.png", "/s/img/{code}.jpg", "/s/img/{code}.gif", "/s/img/{code}.svg"})
    public ResponseEntity<byte[]> proxyImage(@PathVariable String code, HttpServletRequest request) {
        ShortLink link = service.resolve(code);
        if (link == null) {
            return ResponseEntity.notFound().build();
        }
        service.recordVisit(link.getId(), request.getHeader("Referer"), request.getHeader("User-Agent"), IpUtils.getClientIp(request));
        
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    link.getTargetUrl(), HttpMethod.GET, null, byte[].class);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
            if (response.getHeaders().getContentType() != null) {
                headers.setContentType(response.getHeaders().getContentType());
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG);
            }
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
