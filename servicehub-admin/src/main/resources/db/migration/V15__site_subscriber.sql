CREATE TABLE `site_subscriber` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订阅邮箱',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-未验证 1-已验证 2-已退订',
  `verify_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证码',
  `code_expires_at` datetime DEFAULT NULL COMMENT '验证码过期时间',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TOC网站订阅用户表';
