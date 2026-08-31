-- V5：个人网站站点资料与社交名片（hirongbaohub）
CREATE TABLE IF NOT EXISTS site_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    handle VARCHAR(50) NOT NULL,
    bio VARCHAR(500) NULL,
    avatar_url VARCHAR(1024) NULL,
    stat_posts INT NOT NULL DEFAULT 0,
    stat_followers INT NOT NULL DEFAULT 0,
    stat_following INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS site_social (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform VARCHAR(50) NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    url VARCHAR(1024) NULL,
    qr_code_url VARCHAR(1024) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO site_profile (name, handle, bio, avatar_url, stat_posts, stat_followers, stat_following)
VALUES ('hirongbao', 'hirongbao',
        '数字人类学研究者 & 视觉设计师。记录现代交互体验中转瞬即逝的瞬间，探索科技与日常的边界。',
        'https://hirongbao-1321185798.cos.ap-shanghai.myqcloud.com/images/c5cbdb3d-5a5d-4df2-8244-3b8180582ed7.jpg',
        142, 12400, 248);

INSERT INTO site_social (platform, icon_name, url, qr_code_url, sort_order) VALUES
('微信', 'MessageCircle', NULL, 'https://placehold.co/300x300/f8f9fa/18181b?text=WeChat+QR', 1),
('QQ', 'MessageSquare', NULL, 'https://placehold.co/300x300/f8f9fa/18181b?text=QQ+QR', 2),
('抖音', 'Music', NULL, 'https://placehold.co/300x300/f8f9fa/18181b?text=Douyin+QR', 3),
('GitHub', 'Github', 'https://github.com/hirongbao', NULL, 4);
