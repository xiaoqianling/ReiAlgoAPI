-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` VARCHAR(8) PRIMARY KEY COMMENT '用户ID (8位数字)',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `email` VARCHAR(100) NULL UNIQUE COMMENT '邮箱 (允许为空)',
    `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    `avatar_url` VARCHAR(255) NULL COMMENT '头像URL',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_username` (`username`),
    INDEX `idx_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 算法表
CREATE TABLE IF NOT EXISTS `algorithm` (
    `algo_id` VARCHAR(16) PRIMARY KEY COMMENT '算法ID (16位)',
    `user_id` VARCHAR(8) NOT NULL COMMENT '创建者用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '算法标题',
    `description` TEXT NULL COMMENT '算法描述',
    `code_content` LONGTEXT NULL COMMENT '代码内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_public` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开',
    INDEX `idx_algo_user_id` (`user_id`),
    INDEX `idx_algo_title` (`title`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE -- 用户删除时，其算法也删除
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算法信息表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
    `tag_id` VARCHAR(16) PRIMARY KEY COMMENT '标签ID (16位)',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    INDEX `idx_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签信息表';

-- 帖子表
CREATE TABLE IF NOT EXISTS `post` (
    `post_id` VARCHAR(16) PRIMARY KEY COMMENT '帖子ID (16位)',
    `user_id` VARCHAR(8) NOT NULL COMMENT '发布者用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '帖子标题',
    `content` JSON NULL COMMENT '帖子内容 (Slate JS Descendants[] JSON)',
    `views` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_post_user_id` (`user_id`),
    INDEX `idx_post_title` (`title`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE -- 用户删除时，其帖子也删除
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子信息表';

-- 帖子用户评价表 (点赞/点踩)
CREATE TABLE IF NOT EXISTS `post_evaluation` (
    `post_id` VARCHAR(16) NOT NULL COMMENT '帖子ID',
    `user_id` VARCHAR(8) NOT NULL COMMENT '用户ID',
    `evaluation_type` ENUM('LIKE', 'DISLIKE') NOT NULL COMMENT '评价类型',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`post_id`, `user_id`), -- 每个用户对每个帖子只能有一个评价
    INDEX `idx_pe_user_id` (`user_id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`post_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子用户评价表';

-- 帖子与标签关联表 (多对多)
CREATE TABLE IF NOT EXISTS `post_tag` (
    `post_id` VARCHAR(16) NOT NULL COMMENT '帖子ID',
    `tag_id` VARCHAR(16) NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`post_id`, `tag_id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`post_id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `comment_id` VARCHAR(16) PRIMARY KEY COMMENT '评论ID (16位)',
    `post_id` VARCHAR(16) NOT NULL COMMENT '所属帖子ID',
    `user_id` VARCHAR(8) NOT NULL COMMENT '评论者用户ID',
    `parent_comment_id` VARCHAR(16) NULL COMMENT '父评论ID (用于嵌套评论)',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    INDEX `idx_comment_post_id` (`post_id`),
    INDEX `idx_comment_user_id` (`user_id`),
    INDEX `idx_comment_parent_id` (`parent_comment_id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`post_id`) ON DELETE CASCADE, -- 帖子删除时，评论也删除
    FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE, -- 用户删除时，评论也删除
    FOREIGN KEY (`parent_comment_id`) REFERENCES `comment`(`comment_id`) ON DELETE CASCADE -- 父评论删除时，子评论也删除 (可选策略)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论信息表';

-- 文档
CREATE TABLE IF NOT EXISTS `docs`(
    `docs_id` varchar(30) primary key,
    `title` varchar(20) comment '导航栏的名称',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `content` JSON NOT NULL
);