-- V7：个人网站动态表（管理端发布），并把现有展示中的动态迁移入库
CREATE TABLE IF NOT EXISTS site_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(2000) NOT NULL,
    media_type VARCHAR(16) NULL,
    media_url VARCHAR(1024) NULL,
    like_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_status_created (status, created_at)
);

INSERT INTO site_post (content, media_type, media_url, like_count, created_at) VALUES
('周末就是不吃对的，只吃爽的', 'image', 'https://hirongbao-1321185798.cos.ap-shanghai.myqcloud.com/images/9c72405b-3c4a-44f6-b7d1-9c6057d1219c.jpg', 2548, NOW() - INTERVAL 10 MINUTE),
('“最深奥的技术是那些消失的技术。它们将自己编织进日常生活的结构中，直到与生活融为一体。”', NULL, NULL, 342, NOW() - INTERVAL 2 HOUR),
('今天工作室的晨光。非常适合进入深度工作状态，探索全新的视觉系统与交互组件。', 'image', 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=1200', 892, NOW() - INTERVAL 5 HOUR),
('快速浏览一下我们在最新版本中加入的流式动画。注意物理弹簧的阻尼感是如何让整体交互变得自然且生动的。', 'video', 'https://www.w3schools.com/html/mov_bbb.mp4', 1205, NOW() - INTERVAL 1 DAY),
('排版研究：在数字界面设计中，尝试将优雅的衬线斜体与强烈的粗野主义无衬线体进行混合碰撞。', 'image', 'https://images.unsplash.com/photo-1561070791-2526d30994b5?auto=format&fit=crop&q=80&w=1000', 412, NOW() - INTERVAL 2 DAY),
('刚刚发布了我的全新数字花园。全面拥抱留白，移除一切非绝对必要的视觉干扰。少即是多。🤍', NULL, NULL, 890, NOW() - INTERVAL 3 DAY),
('光影游戏：当我们谈论暗黑模式时，我们真正在谈论的是对比度管理的艺术。并非所有的黑都是纯黑，保留一丝环境光的温度。', 'image', 'https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&q=80&w=1200', 633, NOW() - INTERVAL 4 DAY),
('设计工具的演进不应仅仅追求效率，更应探索如何在这个过程中保留创作者的“呼吸感”与“手作感”。', NULL, NULL, 275, NOW() - INTERVAL 5 DAY),
('无意识的日常物件设计，往往蕴含着最顶级的交互逻辑。这是我近期在街头捕捉到的一组城市几何切片。', 'image', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=1200', 1420, NOW() - INTERVAL 7 DAY);
