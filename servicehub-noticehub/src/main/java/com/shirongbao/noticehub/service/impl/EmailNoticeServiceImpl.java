/*
 * auth: hirongbao
 * create: 2026-08-27
 * desc: 邮件通知服务实现类，负责基于模板发送各类系统与业务邮件
 */
package com.shirongbao.noticehub.service.impl;

import lombok.RequiredArgsConstructor;
import com.shirongbao.noticehub.service.NoticeService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailNoticeServiceImpl implements NoticeService {

    private final JavaMailSender mailSender;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${noticehub.site.name:ServiceHub}")
    private String siteName;

    // 发送身份验证验证码邮件
    @Async
    @Override
    public void sendVerificationCode(String email, String code) {
        String subject = "[NoticeHub] 身份验证";
        Map<String, String> variables = Map.of(
                "siteName", siteName,
                "code", code
        );
        String content = renderTemplate("templates/verification-code.html", variables);
        sendHtmlEmail(email, subject, content);
    }

    // 发送最新动态发布订阅通知邮件
    @Async
    @Override
    public void sendPostUpdateNotification(String email, String postTitle, String postExcerpt, String postUrl, String unsubscribeUrl) {
        String subject = "[" + siteName + "] 最新动态发布：" + postTitle;
        Map<String, String> variables = Map.of(
                "siteName", siteName,
                "postTitle", postTitle != null ? postTitle : "",
                "postExcerpt", postExcerpt != null ? postExcerpt : "",
                "postUrl", postUrl != null ? postUrl : "",
                "unsubscribeUrl", unsubscribeUrl != null ? unsubscribeUrl : ""
        );
        String content = renderTemplate("templates/post-update.html", variables);
        sendHtmlEmail(email, subject, content);
    }

    // 发送新访客评论待审核提醒邮件
    @Async
    @Override
    public void sendNewCommentNotification(String email, String postTitle, String author, String commentContent, String ipAddress) {
        String subject = "[NoticeHub] 新评论待审核: " + postTitle;
        String ipStr = (ipAddress != null && !ipAddress.isEmpty()) ? ipAddress : "未知IP";
        String formattedComment = commentContent != null ? commentContent.replace("\n", "<br>") : "";
        Map<String, String> variables = Map.of(
                "postTitle", postTitle != null ? postTitle : "",
                "author", author != null ? author : "",
                "commentContent", formattedComment,
                "ipAddress", ipStr
        );
        String content = renderTemplate("templates/new-comment.html", variables);
        sendHtmlEmail(email, subject, content);
    }

    // 从 classpath 读取模板并渲染变量
    private String renderTemplate(String templatePath, Map<String, String> variables) {
        String template = templateCache.computeIfAbsent(templatePath, this::loadTemplateFromClasspath);
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    // 从 classpath 加载模板文件内容
    private String loadTemplateFromClasspath(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (InputStream in = resource.getInputStream()) {
                return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("加载邮件模板失败: " + templatePath, e);
        }
    }

    // 发送 HTML 格式邮件
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }
}
