-- Reproducible schema for the local MySQL database used by this project.
-- Source of truth: current entity mappings + startup migrations + DB fix endpoints.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `mofadanqing`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `mofadanqing`;

DROP TABLE IF EXISTS `lms_shipment`;
DROP TABLE IF EXISTS `lms_production`;
DROP TABLE IF EXISTS `lms_workshop`;
DROP TABLE IF EXISTS `lms_pack`;
DROP TABLE IF EXISTS `tms_trace_node`;
DROP TABLE IF EXISTS `c2m_design`;
DROP TABLE IF EXISTS `ai_task`;
DROP TABLE IF EXISTS `oms_order_item`;
DROP TABLE IF EXISTS `oms_cart_item`;
DROP TABLE IF EXISTS `oms_order`;
DROP TABLE IF EXISTS `sys_user_message`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `pms_product`;
DROP TABLE IF EXISTS `pms_category`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `nickname` VARCHAR(64) DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `avatar` VARCHAR(500) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `openid` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  UNIQUE KEY `uk_sys_user_email` (`email`),
  UNIQUE KEY `uk_sys_user_openid` (`openid`),
  KEY `idx_sys_user_role` (`role`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pms_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pms_category_status` (`status`),
  KEY `idx_pms_category_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pms_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) DEFAULT NULL,
  `category_id` BIGINT DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `stock` INT NOT NULL DEFAULT 0,
  `model_file` VARCHAR(255) DEFAULT NULL,
  `cover_img` VARCHAR(1000) DEFAULT NULL,
  `description` TEXT,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `c2m_type` VARCHAR(50) NOT NULL DEFAULT 'NONE',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pms_product_category_id` (`category_id`),
  KEY `idx_pms_product_status` (`status`),
  KEY `idx_pms_product_create_time` (`create_time`),
  CONSTRAINT `fk_pms_product_category`
    FOREIGN KEY (`category_id`) REFERENCES `pms_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `operation` VARCHAR(100) NOT NULL,
  `resource_type` VARCHAR(50) DEFAULT NULL,
  `resource_id` BIGINT DEFAULT NULL,
  `old_value` JSON DEFAULT NULL,
  `new_value` JSON DEFAULT NULL,
  `ip_address` VARCHAR(50) DEFAULT NULL,
  `user_agent` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_operation_log_user_id` (`user_id`),
  KEY `idx_sys_operation_log_resource` (`resource_type`, `resource_id`),
  KEY `idx_sys_operation_log_created_at` (`created_at`),
  CONSTRAINT `fk_sys_operation_log_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_user_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `title` VARCHAR(100) DEFAULT NULL,
  `content` VARCHAR(500) DEFAULT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_user_message_user_id` (`user_id`),
  KEY `idx_sys_user_message_is_read` (`is_read`),
  KEY `idx_sys_user_message_created_at` (`created_at`),
  CONSTRAINT `fk_sys_user_message_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oms_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(50) DEFAULT NULL,
  `c2m_status` VARCHAR(50) DEFAULT NULL,
  `payment_method` VARCHAR(20) DEFAULT NULL,
  `payment_time` DATETIME DEFAULT NULL,
  `shipping_address` VARCHAR(255) DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `hair_tracking_no` VARCHAR(100) DEFAULT NULL,
  `finish_tracking_no` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oms_order_order_no` (`order_no`),
  KEY `idx_oms_order_user_id` (`user_id`),
  KEY `idx_oms_order_status` (`status`),
  KEY `idx_oms_order_c2m_status` (`c2m_status`),
  KEY `idx_oms_order_create_time` (`create_time`),
  CONSTRAINT `fk_oms_order_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oms_cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oms_cart_item_user_product` (`user_id`, `product_id`),
  KEY `idx_oms_cart_item_product_id` (`product_id`),
  CONSTRAINT `fk_oms_cart_item_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_oms_cart_item_product`
    FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `oms_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(200) DEFAULT NULL,
  `product_image` VARCHAR(1000) DEFAULT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `c2m_status` VARCHAR(50) DEFAULT NULL,
  `status_timeline` JSON DEFAULT NULL,
  `tracking_no` VARCHAR(100) DEFAULT NULL,
  `c2m_type` VARCHAR(50) DEFAULT NULL,
  `current_status` VARCHAR(50) DEFAULT NULL,
  `media_pack` TEXT,
  `media_workshop` TEXT,
  `media_production` TEXT,
  `media_shipment` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_oms_order_item_order_id` (`order_id`),
  KEY `idx_oms_order_item_product_id` (`product_id`),
  KEY `idx_oms_order_item_current_status` (`current_status`),
  CONSTRAINT `fk_oms_order_item_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_oms_order_item_product`
    FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ai_task` (
  `task_id` VARCHAR(64) NOT NULL,
  `prompt` TEXT,
  `ref_img` VARCHAR(1000) DEFAULT NULL,
  `style` VARCHAR(20) DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT NULL,
  `images` JSON DEFAULT NULL,
  `error_message` TEXT,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  KEY `idx_ai_task_status` (`status`),
  KEY `idx_ai_task_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `c2m_design` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `user_prompt` TEXT,
  `ref_image` VARCHAR(1000) DEFAULT NULL,
  `generated_image` VARCHAR(1000) DEFAULT NULL,
  `style_tag` VARCHAR(50) DEFAULT NULL,
  `layer1_url` VARCHAR(1000) DEFAULT NULL,
  `layer2_url` VARCHAR(1000) DEFAULT NULL,
  `bom_hair_length` DECIMAL(10,2) DEFAULT NULL,
  `bom_silk_weight` DECIMAL(10,2) DEFAULT NULL,
  `estimated_duration` INT DEFAULT NULL,
  `deposit_amount` DECIMAL(10,2) DEFAULT NULL,
  `final_price` DECIMAL(10,2) DEFAULT NULL,
  `balance_due` DECIMAL(10,2) DEFAULT NULL,
  `confirm_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_c2m_design_order_id` (`order_id`),
  CONSTRAINT `fk_c2m_design_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tms_trace_node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `node_type` VARCHAR(20) DEFAULT NULL,
  `title` VARCHAR(50) DEFAULT NULL,
  `description` TEXT,
  `operator_name` VARCHAR(50) DEFAULT NULL,
  `environment_data` JSON DEFAULT NULL,
  `media_url` VARCHAR(1000) DEFAULT NULL,
  `action_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tms_trace_node_order_id` (`order_id`),
  KEY `idx_tms_trace_node_action_time` (`action_time`),
  CONSTRAINT `fk_tms_trace_node_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lms_pack` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT DEFAULT NULL,
  `order_item_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `user_nickname` VARCHAR(64) DEFAULT NULL,
  `sku` VARCHAR(64) DEFAULT NULL,
  `quantity` INT DEFAULT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `logistics_no` VARCHAR(100) DEFAULT NULL,
  `expected_finish_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `media_urls` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lms_pack_order_item_id` (`order_item_id`),
  KEY `idx_lms_pack_order_no` (`order_no`),
  KEY `idx_lms_pack_user_id` (`user_id`),
  KEY `idx_lms_pack_status` (`status`),
  CONSTRAINT `fk_lms_pack_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_pack_order_item`
    FOREIGN KEY (`order_item_id`) REFERENCES `oms_order_item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_pack_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lms_workshop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT DEFAULT NULL,
  `order_item_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `user_nickname` VARCHAR(64) DEFAULT NULL,
  `sku` VARCHAR(64) DEFAULT NULL,
  `quantity` INT DEFAULT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `logistics_no` VARCHAR(100) DEFAULT NULL,
  `expected_finish_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `media_urls` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lms_workshop_order_item_id` (`order_item_id`),
  KEY `idx_lms_workshop_order_no` (`order_no`),
  KEY `idx_lms_workshop_user_id` (`user_id`),
  KEY `idx_lms_workshop_status` (`status`),
  CONSTRAINT `fk_lms_workshop_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_workshop_order_item`
    FOREIGN KEY (`order_item_id`) REFERENCES `oms_order_item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_workshop_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lms_production` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT DEFAULT NULL,
  `order_item_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `user_nickname` VARCHAR(64) DEFAULT NULL,
  `sku` VARCHAR(64) DEFAULT NULL,
  `quantity` INT DEFAULT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `logistics_no` VARCHAR(100) DEFAULT NULL,
  `expected_finish_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `media_urls` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lms_production_order_item_id` (`order_item_id`),
  KEY `idx_lms_production_order_no` (`order_no`),
  KEY `idx_lms_production_user_id` (`user_id`),
  KEY `idx_lms_production_status` (`status`),
  CONSTRAINT `fk_lms_production_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_production_order_item`
    FOREIGN KEY (`order_item_id`) REFERENCES `oms_order_item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_production_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lms_shipment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT DEFAULT NULL,
  `order_item_id` BIGINT DEFAULT NULL,
  `order_no` VARCHAR(64) DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `user_nickname` VARCHAR(64) DEFAULT NULL,
  `sku` VARCHAR(64) DEFAULT NULL,
  `quantity` INT DEFAULT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `logistics_no` VARCHAR(100) DEFAULT NULL,
  `expected_finish_time` DATETIME DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `media_urls` TEXT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lms_shipment_order_item_id` (`order_item_id`),
  KEY `idx_lms_shipment_order_no` (`order_no`),
  KEY `idx_lms_shipment_user_id` (`user_id`),
  KEY `idx_lms_shipment_status` (`status`),
  CONSTRAINT `fk_lms_shipment_order`
    FOREIGN KEY (`order_id`) REFERENCES `oms_order` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_shipment_order_item`
    FOREIGN KEY (`order_item_id`) REFERENCES `oms_order_item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_lms_shipment_user`
    FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
