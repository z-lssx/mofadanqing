-- 电商系统数据库表结构
-- 基于现有架构扩展电商功能

USE mofadanqing;

-- 1. 用户权限表（扩展现有用户表）
ALTER TABLE sys_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT 'USER, ADMIN' AFTER email;
ALTER TABLE sys_user ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE' AFTER role;

-- 2. 商品分类表
CREATE TABLE IF NOT EXISTS pms_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 商品表（扩展现有商品表）
ALTER TABLE pms_product ADD COLUMN category_id BIGINT AFTER name;
ALTER TABLE pms_product ADD COLUMN stock INT DEFAULT 0 COMMENT '库存数量' AFTER price;
ALTER TABLE pms_product ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE' AFTER stock;
ALTER TABLE pms_product ADD COLUMN description TEXT AFTER status;
ALTER TABLE pms_product ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER description;
ALTER TABLE pms_product ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- 添加外键约束
ALTER TABLE pms_product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES pms_category(id);

-- 4. 订单表（扩展现有订单表）
ALTER TABLE oms_order ADD COLUMN order_no VARCHAR(50) UNIQUE AFTER id;
ALTER TABLE oms_order ADD COLUMN total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER user_id;
ALTER TABLE oms_order ADD COLUMN payment_method VARCHAR(50) AFTER total_amount;
ALTER TABLE oms_order ADD COLUMN payment_time TIMESTAMP NULL AFTER payment_method;
ALTER TABLE oms_order ADD COLUMN shipping_address JSON AFTER payment_time;
ALTER TABLE oms_order ADD COLUMN remark TEXT AFTER shipping_address;

-- 修改状态字段为电商标准状态
ALTER TABLE oms_order MODIFY COLUMN status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, PAID, SHIPPED, COMPLETED, CANCELLED';

-- 5. 订单项表
CREATE TABLE IF NOT EXISTS oms_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_image VARCHAR(500),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES oms_order(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES pms_product(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    old_value JSON COMMENT '旧值',
    new_value JSON COMMENT '新值',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 商品库存日志表（用于库存管理）
CREATE TABLE IF NOT EXISTS pms_stock_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    change_quantity INT NOT NULL COMMENT '变化数量',
    old_stock INT NOT NULL COMMENT '变化前库存',
    new_stock INT NOT NULL COMMENT '变化后库存',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：ORDER, ADMIN, REFUND',
    order_id BIGINT COMMENT '关联订单ID',
    remark VARCHAR(200) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES pms_product(id),
    INDEX idx_product_id (product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引优化查询性能
CREATE INDEX idx_product_name ON pms_product(name);
CREATE INDEX idx_product_category ON pms_product(category_id);
CREATE INDEX idx_product_status ON pms_product(status);
CREATE INDEX idx_order_status ON oms_order(status);
CREATE INDEX idx_order_user_id ON oms_order(user_id);
CREATE INDEX idx_order_created_at ON oms_order(create_time);

-- 插入初始数据

-- 商品分类数据
INSERT INTO pms_category (name, description, sort_order) VALUES
('电子产品', '手机、电脑、耳机等电子设备', 1),
('服装配饰', '衣服、鞋子、包包等时尚单品', 2),
('家居用品', '家具、装饰品、生活用品', 3),
('美妆护肤', '化妆品、护肤品、香水', 4),
('运动户外', '运动装备、户外用品、健身器材', 5);

-- 商品数据（扩展现有商品）
UPDATE pms_product SET 
    category_id = 1, 
    stock = 100, 
    status = 'ACTIVE',
    description = '小巧精致，随身携带，承载美好祝愿，采用传统丝绸工艺制作，适合日常使用',
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = 1;

-- 添加更多商品数据
INSERT INTO pms_product (name, category_id, price, stock, description, cover_img, status) VALUES
('iPhone 14 Pro', 1, 7999.00, 50, '苹果iPhone 14 Pro，搭载A16芯片，支持5G网络，拍照效果出色', 'https://example.com/iphone14pro.jpg', 'ACTIVE'),
('MacBook Air M2', 1, 8999.00, 30, '苹果MacBook Air M2芯片版，轻薄便携，续航持久，适合办公学习', 'https://example.com/macbookair.jpg', 'ACTIVE'),
('AirPods Pro 2', 1, 1899.00, 200, '苹果AirPods Pro第二代，主动降噪，空间音频，音质出色', 'https://example.com/airpodspro2.jpg', 'ACTIVE'),
('时尚连衣裙', 2, 299.00, 150, '优雅时尚连衣裙，适合多种场合穿着，面料舒适，版型修身', 'https://example.com/dress.jpg', 'ACTIVE'),
('运动鞋', 2, 599.00, 100, '舒适透气运动鞋，适合跑步健身，缓震效果好，款式时尚', 'https://example.com/sneakers.jpg', 'ACTIVE'),
('智能手表', 1, 1299.00, 80, '多功能智能手表，支持健康监测、运动追踪、消息提醒', 'https://example.com/smartwatch.jpg', 'ACTIVE');

-- 更新管理员权限
UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin';

-- 为luo用户创建订单数据
INSERT INTO oms_order (id, order_no, user_id, total_amount, status, payment_method, payment_time, shipping_address, remark, create_time, update_time) VALUES
(10001, 'ORD202402010001', 2, 2999.00, 'COMPLETED', 'alipay', DATE_SUB(NOW(), INTERVAL 30 DAY), '{"receiver":"罗先生","phone":"13800138000","address":"北京市朝阳区科技园区100号","zipCode":"100000"}', '用户定制订单，已发货', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(10002, 'ORD202402010002', 2, 1899.00, 'PAID', 'wechat', DATE_SUB(NOW(), INTERVAL 20 DAY), '{"receiver":"罗先生","phone":"13800138000","address":"北京市朝阳区科技园区100号","zipCode":"100000"}', '等待发货', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10003, 'ORD202402010003', 2, 5999.00, 'SHIPPED', 'alipay', DATE_SUB(NOW(), INTERVAL 15 DAY), '{"receiver":"罗先生","phone":"13800138000","address":"北京市朝阳区科技园区100号","zipCode":"100000"}', '已发货，预计3天内送达', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10004, 'ORD202402010004', 2, 299.00, 'PENDING', NULL, NULL, '{"receiver":"罗先生","phone":"13800138000","address":"北京市朝阳区科技园区100号","zipCode":"100000"}', '待支付，请及时付款', NOW(), NOW());

-- 插入订单项数据
INSERT INTO oms_order_item (order_id, product_id, product_name, product_image, quantity, unit_price, subtotal) VALUES
(10001, 2, '案头雅玩', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20round%20fan%20with%20silk%20embroidery&image_size=square', 1, 2999.00, 2999.00),
(10002, 7, 'AirPods Pro 2', 'https://example.com/airpodspro2.jpg', 1, 1899.00, 1899.00),
(10003, 3, 'iPhone 14 Pro', 'https://example.com/iphone14pro.jpg', 1, 5999.00, 5999.00),
(10004, 8, '时尚连衣裙', 'https://example.com/dress.jpg', 1, 299.00, 299.00);

-- 插入操作日志数据
INSERT INTO sys_operation_log (user_id, operation, resource_type, resource_id, old_value, new_value, ip_address, remark) VALUES
(1, 'UPDATE_USER_ROLE', 'USER', 1, '{"role":"USER"}', '{"role":"ADMIN"}', '192.168.1.100', '将admin用户升级为管理员'),
(1, 'CREATE_PRODUCT', 'PRODUCT', 3, NULL, '{"name":"iPhone 14 Pro","price":7999,"stock":50}', '192.168.1.100', '管理员添加新商品'),
(1, 'UPDATE_ORDER_STATUS', 'ORDER', 10001, '{"status":"PAID"}', '{"status":"COMPLETED"}', '192.168.1.100', '管理员完成订单'),
(2, 'CREATE_ORDER', 'ORDER', 10004, NULL, '{"status":"PENDING","totalAmount":299}', '192.168.1.1', '用户创建新订单');

-- 插入库存变更日志
INSERT INTO pms_stock_log (product_id, change_quantity, old_stock, new_stock, operation_type, order_id, remark) VALUES
(2, -1, 100, 99, 'ORDER', 10001, '订单消耗库存'),
(7, -1, 200, 199, 'ORDER', 10002, '订单消耗库存'),
(3, -1, 50, 49, 'ORDER', 10003, '订单消耗库存'),
(8, -1, 150, 149, 'ORDER', 10004, '订单消耗库存');