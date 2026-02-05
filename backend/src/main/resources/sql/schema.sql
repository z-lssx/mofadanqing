-- 创建数据库
CREATE DATABASE IF NOT EXISTS mofadanqing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mofadanqing;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(128) NOT NULL,
  `phone` varchar(20),
  `email` varchar(100),
  `avatar` varchar(255),
  `role` varchar(20) DEFAULT 'USER',
  `status` varchar(20) DEFAULT 'ACTIVE',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 兼容已有表结构，补充缺失列
ALTER TABLE `sys_user` ADD COLUMN `role` varchar(20) DEFAULT 'USER';
ALTER TABLE `sys_user` ADD COLUMN `status` varchar(20) DEFAULT 'ACTIVE';

-- 2. 商品系列表
CREATE TABLE IF NOT EXISTS `pms_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `sort_order` int DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `pms_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COMMENT '商品名称',
  `category_id` bigint,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `model_file` varchar(255) COMMENT '3D模型.glb文件地址',
  `cover_img` varchar(255) COMMENT '封面图URL',
  `description` text,
  `status` varchar(20) DEFAULT 'ACTIVE',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `pms_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 兼容已有表，增加缺失列及索引
ALTER TABLE `pms_product` ADD COLUMN `category_id` bigint;
ALTER TABLE `pms_product` ADD COLUMN `stock` int NOT NULL DEFAULT 0;
ALTER TABLE `pms_product` ADD COLUMN `status` varchar(20) DEFAULT 'ACTIVE';
CREATE INDEX `idx_category_id` ON `pms_product`(`category_id`);
CREATE INDEX `idx_status` ON `pms_product`(`status`);
CREATE INDEX `idx_name` ON `pms_product`(`name`);

-- 3. 订单主表
CREATE TABLE IF NOT EXISTS `oms_order` (
  `id` bigint NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `status` int COMMENT '0:待付 1:待寄发 2:已寄发 3:工坊收 4:制作中 5:已发货 6:完成',
  `total_amount` decimal(10,2),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `hair_tracking_no` varchar(64) COMMENT '用户寄头发的快递单号',
  `finish_tracking_no` varchar(64) COMMENT '成品发货快递单号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单项表
CREATE TABLE IF NOT EXISTS `oms_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(200),
  `product_image` varchar(255),
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. C2M 定制详情表 (关联订单)
CREATE TABLE IF NOT EXISTS `c2m_design` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `user_prompt` text COMMENT '用户AI关键词',
  `ref_image` varchar(255) COMMENT '用户上传参考图',
  `generated_image` varchar(255) COMMENT '最终确认的AI刺绣稿',
  `style_tag` varchar(50) COMMENT '风格：工笔/写意',
  `layer1_url` varchar(255) COMMENT '骨架层/线稿层URL',
  `layer2_url` varchar(255) COMMENT '润色层/丝绸层URL',
  `bom_hair_length` decimal(10,2) COMMENT '发丝用量(m)',
  `bom_silk_weight` decimal(10,2) COMMENT '蚕丝用量(g)',
  `estimated_duration` int COMMENT '预计工期(天)',
  `deposit_amount` decimal(10,2) COMMENT '定金金额',
  `final_price` decimal(10,2) COMMENT '最终总价',
  `balance_due` decimal(10,2) COMMENT '待付尾款',
  `confirm_time` datetime COMMENT '定稿确认时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  CONSTRAINT `fk_design_order` FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 溯源节点记录表 (Timeline)
CREATE TABLE IF NOT EXISTS `tms_trace_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `node_type` varchar(20) COMMENT 'PACK, RECEIVE, MAKING, SHIP',
  `title` varchar(50) COMMENT '节点标题',
  `description` text COMMENT '节点描述',
  `operator_name` varchar(50) COMMENT '操作人/匠人',
  `environment_data` json COMMENT '温湿度数据 {"temp":24, "hum":45}',
  `media_url` varchar(255) COMMENT '节点图片或直播回放',
  `action_time` datetime,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `fk_trace_order` FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始数据

-- 商品系列数据
INSERT INTO `pms_category` (`name`, `status`, `sort_order`) VALUES
('随身锦囊', 'ACTIVE', 1),
('案头雅玩', 'ACTIVE', 2),
('传世典藏', 'ACTIVE', 3);

INSERT INTO `pms_product` (`name`, `category_id`, `price`, `stock`, `model_file`, `cover_img`, `description`, `status`) VALUES
('随身锦囊·经典款', 1, 1999.00, 100, 'models/pouch.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=traditional%20chinese%20silk%20pouch%20with%20embroidery&image_size=square', '小巧精致，随身携带，承载美好祝愿', 'ACTIVE'),
('案头雅玩·团扇', 2, 2999.00, 80, 'models/fan.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20round%20fan%20with%20silk%20embroidery&image_size=square', '精致典雅，置于案头，彰显文化品味', 'ACTIVE'),
('传世典藏·卷轴', 3, 5999.00, 50, 'models/scroll.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20silk%20scroll%20painting%20with%20embroidery&image_size=square', '精工细作，传承经典，值得珍藏传世', 'ACTIVE');

-- 6. AI 任务表
CREATE TABLE IF NOT EXISTS `ai_task` (
  `task_id` varchar(64) NOT NULL COMMENT '任务ID',
  `prompt` text COMMENT '用户关键词',
  `ref_img` varchar(255) COMMENT '参考图URL',
  `style` varchar(20) COMMENT '风格：TRADITIONAL, MODERN',
  `status` varchar(20) COMMENT '状态：PENDING, COMPLETED, FAILED',
  `images` json COMMENT '生成的图片URL列表',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint,
  `operation` varchar(100) NOT NULL,
  `resource_type` varchar(50),
  `resource_id` bigint,
  `old_value` json,
  `new_value` json,
  `ip_address` varchar(50),
  `user_agent` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 测试用户数据
-- 密码为：123456
INSERT INTO `sys_user` (`username`, `password`, `phone`, `email`, `role`) VALUES
('admin', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138000', 'admin@example.com', 'ADMIN')
ON DUPLICATE KEY UPDATE `role`='ADMIN';

-- 普通用户
-- 密码为：123456
INSERT INTO `sys_user` (`username`, `password`, `phone`, `email`, `role`) VALUES
('user', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13900139000', 'user@example.com', 'USER')
ON DUPLICATE KEY UPDATE `role`='USER';

INSERT INTO `sys_user` (`username`, `password`, `phone`, `email`, `role`) VALUES
('luo', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13900139001', 'luo@example.com', 'USER')
ON DUPLICATE KEY UPDATE `role`='USER';

-- 注意：密码已使用 BCrypt 加密，明文密码为 123456
