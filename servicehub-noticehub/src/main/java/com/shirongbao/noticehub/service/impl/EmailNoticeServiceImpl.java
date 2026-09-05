package com.shirongbao.noticehub.service.impl;

import com.shirongbao.noticehub.service.NoticeService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNoticeServiceImpl implements NoticeService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${noticehub.site.name:ServiceHub}")
    private String siteName;

    @Async
    @Override
    public void sendVerificationCode(String email, String code) {
        String subject = "[" + siteName + "] 订阅验证码";
        String content = "<html><body>" +
                "<h2 style='color:#18181b;'>欢迎订阅 " + siteName + "</h2>" +
                "<p>您正在申请订阅我们的最新动态，您的验证码是：<strong style='font-size:24px;color:#18181b;'>" + code + "</strong></p>" +
                "<p style='color:#71717a;font-size:12px;'>该验证码将在 15 分钟后过期，请尽快完成验证。</p>" +
                "</body></html>";
        sendHtmlEmail(email, subject, content);
    }

    @Async
    @Override
    public void sendPostUpdateNotification(String email, String postTitle, String postExcerpt, String postUrl) {
        String subject = "[" + siteName + "] 新动态通知：" + postTitle;
        String content = "<html><body style='font-family: sans-serif; color: #18181b;'>" +
                "<h2>" + siteName + " 发布了新动态</h2>" +
                "<div style='border-left: 4px solid #18181b; padding-left: 16px; margin: 24px 0;'>" +
                "<h3 style='margin-top:0;'>" + postTitle + "</h3>" +
                "<p style='color: #52525b;'>" + postExcerpt + "</p>" +
                "</div>" +
                "<a href='" + postUrl + "' style='display:inline-block; padding: 12px 24px; background-color: #18181b; color: #fff; text-decoration: none; border-radius: 9999px; font-weight: bold;'>立即查看</a>" +
                "<p style='margin-top: 40px; font-size: 12px; color: #a1a1aa;'>如果您不想再接收此类邮件，您可以联系我们取消订阅。</p>" +
                "</body></html>";
        sendHtmlEmail(email, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
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
