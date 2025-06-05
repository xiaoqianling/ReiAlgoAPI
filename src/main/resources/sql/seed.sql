-- 种子数据

-- 添加管理员用户 (密码: password)
-- 注意: user_id '10000001' 和 password_hash 需要替换为实际生成的值
INSERT INTO `user` (`user_id`, `username`, `password`, `email`, `role`, `avatar_url`, `created_at`) VALUES
('10000001', 'admin', '$2a$10$NZFB..r7j.OM4Lp6j0V0fuU/XyYJ/9T.D7iR.h.L3yG.5iX8f/4Jy', 'admin@example.com', 'ADMIN', NULL, NOW());

-- 添加普通用户 (密码: password123)
-- 注意: user_id '87654321' 和 password_hash 需要替换为实际生成的值
INSERT INTO `user` (`user_id`, `username`, `password`, `email`, `role`, `avatar_url`, `created_at`) VALUES
('87654321', 'testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'test@example.com', 'USER', 'https://example.com/avatar.png', NOW());

-- 添加更多普通用户 (密码: password)
INSERT INTO `user` (`user_id`, `username`, `password`, `email`, `role`, `avatar_url`, `created_at`) VALUES
('10000002', 'alice', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'alice@sample.net', 'USER', NULL, NOW()),
('10000003', 'bob', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bob@sample.org', 'USER', 'https://sample.org/bob_avatar.jpg', NOW());

-- 添加一些标签
-- 注意: tag_id 需要替换为实际生成的值
INSERT INTO `tag` (`tag_id`, `name`) VALUES
('cINkkcgiaGgZ5Vwi', 'Java'),
('PTHWbRzA6l3xehHf', 'Spring Boot'),
('DlfZBMPzEQmFUNER', 'MyBatis'),
('Z15ScunB2vk9dwg3', 'Algorithm'),
('8f8qzE4iBFMyzcHS', 'Web Development');

-- 添加示例帖子 (使用上面用户的 user_id)
-- 注意: post_id 需要替换为实际生成的值
-- Post 1 by testuser
INSERT INTO `post` (`post_id`, `user_id`, `title`, `content`, `created_at`, `updated_at`) VALUES
('qgHYo8Vp72HE30G5', '87654321', 'My First Post with Slate JS', '[{"type":"paragraph","children":[{"text":"This is the content of my first post using Slate JS format!"}]}]', NOW(), NOW());

-- Post 2 by alice
INSERT INTO `post` (`post_id`, `user_id`, `title`, `content`, `created_at`, `updated_at`) VALUES
('ajFxPGR0YygCOE45', '10000002', 'Learning Spring Boot', '[{"type":"paragraph","children":[{"text":"Getting started with Spring Boot is quite easy."},{"type":"paragraph","children":[{"text":"Need to understand Beans and Dependency Injection."}]}]}]', NOW(), NOW());

-- Post 3 by bob (No content)
INSERT INTO `post` (`post_id`, `user_id`, `title`, `content`, `created_at`, `updated_at`) VALUES
('ZRt7h1S6sAXmzPII', '10000003', 'Database Design Principles', NULL, NOW(), NOW());

-- 添加帖子与标签的关联
INSERT INTO `post_tag` (`post_id`, `tag_id`) VALUES
('ZRt7h1S6sAXmzPII', '8f8qzE4iBFMyzcHS'), -- Post 1: Web Development
('ajFxPGR0YygCOE45', 'DlfZBMPzEQmFUNER'), -- Post 1: React (Assuming Slate means React usage)
('qgHYo8Vp72HE30G5', 'cINkkcgiaGgZ5Vwi'); -- Post 2: Java

-- 添加评论
-- 注意: comment_id 需要替换为实际生成的值
-- Comment 1 on Post 1 by bob
INSERT INTO `comment` (`comment_id`, `post_id`, `user_id`, `parent_comment_id`, `content`, `created_at`) VALUES
('atmFmiIo9aiL9ihF', 'ZRt7h1S6sAXmzPII', '10000001', NULL, 'Great first post!', NOW());
