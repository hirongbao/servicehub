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
        String subject = "[NoticeHub] 身份验证";
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f4f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding: 40px 0; background-color: #f4f4f5;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"500\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); overflow: hidden; margin: 0 20px;\">\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 30px 40px; border-bottom: 1px solid #f4f4f5;\">\n" +
                "                            <div style=\"font-size: 16px; font-weight: 700; color: #18181b; letter-spacing: -0.5px;\">NoticeHub <span style=\"color: #a1a1aa; font-weight: 400;\">/ 验证中心</span></div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px;\">\n" +
                "                            <h2 style=\"margin: 0 0 20px 0; font-size: 20px; color: #18181b; font-weight: 600;\">身份验证申请</h2>\n" +
                "                            <p style=\"margin: 0 0 30px 0; font-size: 14px; color: #52525b; line-height: 1.6;\">\n" +
                "                                您正在执行一项敏感操作，需要验证您的电子邮件地址。该请求的调用方信息如下：\n" +
                "                            </p>\n" +
                "                            <div style=\"background-color: #fafafa; border-radius: 8px; padding: 16px; margin-bottom: 30px; border: 1px solid #f4f4f5;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                                    <tr>\n" +
                "                                        <td width=\"80\" style=\"font-size: 13px; color: #a1a1aa;\">业务来源</td>\n" +
                "                                        <td style=\"font-size: 14px; font-weight: 600; color: #18181b;\">" + siteName + ".com</td>\n" +
                "                                    </tr>\n" +
                "                                    <tr><td colspan=\"2\" style=\"height: 12px;\"></td></tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-size: 13px; color: #a1a1aa;\">平台环境</td>\n" +
                "                                        <td style=\"font-size: 14px; font-weight: 500; color: #52525b;\">" + siteName + " 官方网站</td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </div>\n" +
                "                            <p style=\"margin: 0 0 12px 0; font-size: 13px; color: #71717a;\">验证码 (15 分钟内有效):</p>\n" +
                "                            <div style=\"background-color: #18181b; border-radius: 8px; padding: 24px; text-align: center; margin-bottom: 30px;\">\n" +
                "                                <span style=\"font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 36px; font-weight: 700; color: #ffffff; letter-spacing: 16px; margin-left: 16px;\">" + code + "</span>\n" +
                "                            </div>\n" +
                "                            <p style=\"margin: 0; font-size: 12px; color: #a1a1aa; line-height: 1.5;\">\n" +
                "                                如果您未在 " + siteName + " 发起过此请求，请直接忽略此邮件，您的账号环境目前依然安全。\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #fafafa; padding: 24px 40px; text-align: center; border-top: 1px solid #f4f4f5;\">\n" +
                "                            <p style=\"margin: 0; font-size: 12px; color: #a1a1aa;\">\n" +
                "                                本验证邮件由基础通知服务 <strong style=\"color: #71717a;\">NoticeHub</strong> 自动投递。<br>请勿直接回复此邮件。\n" +
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
        String subject = "[" + siteName + "] 最新动态发布：" + postTitle;
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f4f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding: 60px 0; background-color: #f4f4f5;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.06); overflow: hidden; margin: 0 20px;\">\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 48px 48px 0 48px; text-align: center;\">\n" +
                "                            <div style=\"display: inline-block; padding: 6px 16px; background-color: #f4f4f5; border-radius: 999px; font-size: 11px; font-weight: 700; color: #a1a1aa; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 24px;\">\n" +
                "                                动 态 更 新\n" +
                "                            </div>\n" +
                "                            <h2 style=\"margin: 0; font-family: Georgia, 'Times New Roman', Times, serif; font-size: 24px; font-style: italic; color: #18181b; font-weight: normal;\">\n" +
                "                                " + siteName + "\n" +
                "                            </h2>\n" +
                "                            <div style=\"margin-top: 16px; width: 32px; height: 1px; background-color: #e4e4e7; display: inline-block;\"></div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 48px 56px 48px;\">\n" +
                "                            <h1 style=\"margin: 0 0 24px 0; font-size: 24px; line-height: 1.5; color: #18181b; font-weight: 600; text-align: center;\">\n" +
                "                                " + postTitle + "\n" +
                "                            </h1>\n" +
                "                            <div style=\"border-left: 3px solid #18181b; padding-left: 20px; margin: 0 0 40px 0;\">\n" +
                "                                <p style=\"margin: 0; font-size: 15px; line-height: 1.8; color: #52525b; font-weight: 300;\">\n" +
                "                                    " + postExcerpt + "\n" +
                "                                </p>\n" +
                "                            </div>\n" +
                "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\">\n" +
                "                                        <a href=\"" + postUrl + "\" style=\"display: inline-block; padding: 16px 36px; background-color: #18181b; color: #ffffff; text-decoration: none; border-radius: 9999px; font-size: 13px; font-weight: 600; letter-spacing: 1px;\">\n" +
                "                                            前往 " + siteName + ".com 查看完整动态\n" +
                "                                        </a>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #fafafa; border-top: 1px solid #f4f4f5; padding: 40px 48px; text-align: center;\">\n" +
                "                            <p style=\"margin: 0 0 16px 0; font-size: 12px; color: #a1a1aa; line-height: 1.6;\">\n" +
                "                                本通知由底座服务 <span style=\"font-weight: 600; color: #71717a;\">NoticeHub</span> 提供推送能力。<br>\n" +
                "                                您接收到此邮件是因为您曾在 <strong style=\"color: #71717a;\">" + siteName + "</strong> 留下了您的订阅邮箱。\n" +
                "                            </p>\n" +
                "                            <a href=\"" + unsubscribeUrl + "\" style=\"font-size: 12px; color: #71717a; text-decoration: underline;\">退订此动态通知</a>\n" +
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
    public void sendNewCommentNotification(String email, String postTitle, String author, String commentContent, String ipAddress) {
        String subject = "[NoticeHub] 新评论待审核: " + postTitle;
        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f4f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\">\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"padding: 40px 0; background-color: #f4f4f5;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table width=\"500\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); overflow: hidden; margin: 0 20px;\">\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 30px 40px; border-bottom: 1px solid #f4f4f5; background-color: #fafafa;\">\n" +
                "                            <div style=\"font-size: 14px; font-weight: 700; color: #18181b; letter-spacing: 1px; text-transform: uppercase;\">NoticeHub / 审核中心</div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px;\">\n" +
                "                            <h2 style=\"margin: 0 0 24px 0; font-size: 20px; color: #18181b; font-weight: 600; border-left: 4px solid #18181b; padding-left: 12px;\">新访客评论</h2>\n" +
                "                            <p style=\"margin: 0 0 24px 0; font-size: 14px; color: #52525b; line-height: 1.6;\">\n" +
                "                                您的网站动态 <strong style=\"color: #18181b;\">" + postTitle + "</strong> 收到了一条新评论，需等待您的审核。\n" +
                "                            </p>\n" +
                "                            <div style=\"background-color: #fafafa; border-radius: 8px; padding: 20px; margin-bottom: 30px; border: 1px solid #e4e4e7;\">\n" +
                "                                <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                                    <tr>\n" +
                "                                        <td width=\"60\" style=\"font-size: 13px; color: #a1a1aa; vertical-align: top;\">访客</td>\n" +
                "                                        <td style=\"font-size: 14px; font-weight: 600; color: #18181b; padding-bottom: 12px;\">" + author + "</td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-size: 13px; color: #a1a1aa; vertical-align: top;\">内容</td>\n" +
                "                                        <td style=\"font-size: 14px; font-weight: 400; color: #52525b; line-height: 1.6; padding-bottom: 12px;\">\n" +
                "                                            " + commentContent.replace("\n", "<br>") + "\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-size: 13px; color: #a1a1aa; vertical-align: top;\">IP</td>\n" +
                "                                        <td style=\"font-size: 13px; font-weight: 500; color: #a1a1aa; font-family: monospace;\">" + (ipAddress != null ? ipAddress : "未知") + "</td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </div>\n" +
                "                            <div style=\"text-align: center;\">\n" +
                "                                <a href=\"https://admin.hirongbao.com\" style=\"display: inline-block; padding: 12px 32px; background-color: #18181b; color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 13px; font-weight: 600;\">前往后台审核</a>\n" +
                "                            </div>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #fafafa; padding: 24px 40px; text-align: center; border-top: 1px solid #f4f4f5;\">\n" +
                "                            <p style=\"margin: 0; font-size: 12px; color: #a1a1aa;\">\n" +
                "                                内部管理通知由 <strong style=\"color: #71717a;\">NoticeHub</strong> 自动投递。\n" +
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
