package com.shirongbao.noticehub.service;

public interface NoticeService {
    void sendVerificationCode(String email, String code);
    void sendPostUpdateNotification(String email, String postTitle, String postExcerpt, String postUrl, String unsubscribeUrl);
    void sendNewCommentNotification(String email, String postTitle, String author, String commentContent, String ipAddress);
}
