-- Base reproducible data for local development.
-- After importing this file, start the backend once so StartupInitializer
-- can automatically backfill demo orders and logistics records when needed.

SET NAMES utf8mb4;
USE `mofadanqing`;

INSERT INTO `sys_user`
(`id`, `username`, `nickname`, `password`, `phone`, `email`, `avatar`, `role`, `status`, `openid`, `create_time`, `update_time`)
VALUES
(1, 'admin', 'admin', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138000', 'admin@example.com', NULL, 'ADMIN', 'ACTIVE', NULL, NOW(), NOW()),
(2, 'user', 'user', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13900139000', 'user@example.com', NULL, 'USER', 'ACTIVE', NULL, NOW(), NOW()),
(3, 'luo', 'luo', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13900139001', 'luo@example.com', NULL, 'USER', 'ACTIVE', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE
`nickname` = VALUES(`nickname`),
`password` = VALUES(`password`),
`phone` = VALUES(`phone`),
`email` = VALUES(`email`),
`role` = VALUES(`role`),
`status` = VALUES(`status`),
`update_time` = NOW();

INSERT INTO `pms_category`
(`id`, `name`, `description`, `sort_order`, `status`, `created_at`, `updated_at`)
VALUES
(1, 'Pouch', 'Portable embroidered products.', 1, 'ACTIVE', NOW(), NOW()),
(2, 'Desk Art', 'Display-friendly handmade products.', 2, 'ACTIVE', NOW(), NOW()),
(3, 'Collection', 'High-value collectible products.', 3, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`sort_order` = VALUES(`sort_order`),
`status` = VALUES(`status`),
`updated_at` = NOW();

INSERT INTO `pms_product`
(`id`, `name`, `category_id`, `price`, `stock`, `model_file`, `cover_img`, `description`, `status`, `c2m_type`, `create_time`, `update_time`)
VALUES
(1, 'Pouch Classic', 1, 1999.00, 100, 'models/pouch.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=traditional%20chinese%20silk%20pouch%20with%20embroidery&image_size=square', 'Compact embroidered pouch for everyday carry.', 'ACTIVE', 'NONE', NOW(), NOW()),
(2, 'Round Fan', 2, 2999.00, 80, 'models/fan.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20round%20fan%20with%20silk%20embroidery&image_size=square', 'Decorative round fan with silk embroidery.', 'ACTIVE', 'NONE', NOW(), NOW()),
(3, 'Scroll Collection', 3, 5999.00, 50, 'models/scroll.glb', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20silk%20scroll%20painting%20with%20embroidery&image_size=square', 'Collectible scroll-style embroidery artwork.', 'ACTIVE', 'NONE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`category_id` = VALUES(`category_id`),
`price` = VALUES(`price`),
`stock` = VALUES(`stock`),
`model_file` = VALUES(`model_file`),
`cover_img` = VALUES(`cover_img`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`c2m_type` = VALUES(`c2m_type`),
`update_time` = NOW();

INSERT INTO `sys_user_message`
(`id`, `user_id`, `title`, `content`, `is_read`, `created_at`)
VALUES
(1, 2, 'Welcome', 'Base demo data has been imported. Start the backend once to backfill more demo records.', 0, NOW()),
(2, 3, 'Demo Account', 'The luo account is ready for local frontend and backend verification.', 0, NOW())
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`),
`content` = VALUES(`content`),
`is_read` = VALUES(`is_read`);
