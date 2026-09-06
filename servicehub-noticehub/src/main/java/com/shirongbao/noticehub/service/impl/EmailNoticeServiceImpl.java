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
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #fafafa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #fafafa; padding: 60px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,0.04); margin: 0 20px;\">\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 56px 48px;\">\n" +
                "                            <h1 style=\"margin: 0 0 24px 0; font-size: 32px; font-weight: normal; font-family: Georgia, 'Times New Roman', Times, serif; font-style: italic; color: #18181b;\">Subscribe</h1>\n" +
                "                            <p style=\"margin: 0 0 40px 0; font-size: 15px; line-height: 1.8; color: #52525b; font-weight: 300;\">\n" +
                "                                欢迎订阅 <strong style=\"color: #18181b;\">" + siteName + "</strong>。<br>您正在申请获取最新数字动态与灵感更新，这是您的验证码：\n" +
                "                            </p>\n" +
                "                            <div style=\"background-color: #fafafa; border: 1px solid #e4e4e7; border-radius: 16px; padding: 32px; text-align: center; margin-bottom: 40px;\">\n" +
                "                                <span style=\"font-size: 40px; font-weight: 700; color: #18181b; letter-spacing: 12px; margin-left: 12px;\">" + code + "</span>\n" +
                "                            </div>\n" +
                "                            <p style=\"margin: 0; font-size: 13px; color: #a1a1aa; line-height: 1.6;\">\n" +
                "                                该验证码将在 15 分钟后过期。<br>如果您没有请求此代码，请忽略此电子邮件。\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #18181b; padding: 32px 48px; text-align: center;\">\n" +
                "                            <p style=\"margin: 0; font-size: 11px; color: #71717a; text-transform: uppercase; letter-spacing: 3px;\">\n" +
                "                                © " + java.time.Year.now().getValue() + " " + siteName + " ALL RIGHTS RESERVED.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
        sendHtmlEmail(email, subject, content);
    }

    @Async
    @Override
    public void sendPostUpdateNotification(String email, String postTitle, String postExcerpt, String postUrl, String unsubscribeUrl) {
        String subject = "[" + siteName + "] 新动态：" + postTitle;
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #fafafa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #fafafa; padding: 60px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,0.04); margin: 0 20px;\">\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 56px 48px;\">\n" +
                "                            <div style=\"font-size: 11px; font-weight: bold; text-transform: uppercase; letter-spacing: 3px; color: #a1a1aa; margin-bottom: 24px;\">New Update</div>\n" +
                "                            <h1 style=\"margin: 0 0 32px 0; font-size: 28px; line-height: 1.4; font-weight: normal; font-family: Georgia, 'Times New Roman', Times, serif; color: #18181b;\">" + postTitle + "</h1>\n" +
                "                            <div style=\"border-left: 3px solid #f4f4f5; padding-left: 24px; margin: 0 0 40px 0;\">\n" +
                "                                <p style=\"margin: 0; font-size: 15px; line-height: 1.8; color: #52525b; font-weight: 300;\">\n" +
                "                                    " + postExcerpt + "\n" +
                "                                </p>\n" +
                "                            </div>\n" +
                "                            <div style=\"text-align: left;\">\n" +
                "                                <a href=\"" + postUrl + "\" style=\"display: inline-block; padding: 16px 32px; background-color: #18181b; color: #ffffff; text-decoration: none; border-radius: 9999px; font-size: 12px; font-weight: bold; text-transform: uppercase; letter-spacing: 2px;\">阅读完整动态</a>\n" +
                "                            </div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #18181b; padding: 40px 48px; text-align: center;\">\n" +
                "                            <p style=\"margin: 0 0 16px 0; font-size: 13px; color: #71717a;\">\n" +
                "                                您收到此邮件是因为您订阅了 " + siteName + "。<br><a href=\"" + unsubscribeUrl + "\" style=\"color: #a1a1aa; text-decoration: underline; margin-top: 8px; display: inline-block;\">退订通知</a>\n" +
                "                            </p>\n" +
                "                            <p style=\"margin: 0; font-size: 11px; color: #52525b; text-transform: uppercase; letter-spacing: 3px;\">\n" +
                "                                © " + java.time.Year.now().getValue() + " " + siteName + ". ALL RIGHTS RESERVED.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
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
