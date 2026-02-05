USE mofadanqing;

-- 为用户 luo 创建订单和溯源数据

-- 1. 获取用户 luo 的 ID
INSERT IGNORE INTO sys_user (username, password, email, role) VALUES ('luo', '$2a$10$EIXZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'luo@example.com', 'USER');
SET @user_id = (SELECT id FROM sys_user WHERE username = 'luo');
SET @product_id = (SELECT id FROM pms_product WHERE name LIKE '随身锦囊%' ORDER BY id LIMIT 1);

-- 2. 创建订单数据
INSERT INTO `oms_order` (`id`, `user_id`, `product_id`, `status`, `total_amount`, `create_time`) VALUES
-- 处理中的订单
(202512180099, @user_id, @product_id, 4, 1999.00, '2025-12-18 10:30:00'),
-- 已完成的订单
(202512100088, @user_id, @product_id, 6, 1999.00, '2025-12-10 15:20:00'),
-- 已完成的订单
(202511250077, @user_id, @product_id, 6, 1999.00, '2025-11-25 09:45:00');

-- 3. 创建 C2M 设计数据
INSERT INTO `c2m_design` (`order_id`, `user_prompt`, `style_tag`) VALUES
(202512180099, '传统纹样，连理枝，红色', '工笔'),
(202512100088, '古典花卉，牡丹，粉色', '工笔'),
(202511250077, '传统纹样，龙凤呈祥，金色', '工笔');

-- 4. 创建溯源节点数据

-- 订单 202512180099 的溯源节点（处理中）
INSERT INTO `tms_trace_node` (`order_id`, `node_type`, `title`, `description`, `operator_name`, `environment_data`, `media_url`, `action_time`) VALUES
(202512180099, 'pack', '采发包寄出', '用户头发采发包已寄出，快递单号：SF1234567890', '系统', '{"temp": 22, "hum": 45}', 'https://images.unsplash.com/photo-1605367332306-056158223dc7?q=80&w=200', '2025-12-18 10:30:00'),
(202512180099, 'receive', '工坊签收', '工坊已收到用户头发采发包，开始准备制作', '李师傅', '{"temp": 23, "hum": 48}', 'https://images.unsplash.com/photo-1531238886364-7541f6087593?q=80&w=200', '2025-12-20 09:15:00'),
(202512180099, 'making', '绣制中', '非遗传承人李绣娘正在进行核心图案"连理枝"的绣制，使用了传统的"齐针"技法', '李绣娘', '{"temp": 24, "hum": 50}', 'https://images.unsplash.com/photo-1544967082-d9d37d879482?q=80&w=200', '2025-12-22 14:20:00'),
(202512180099, 'ship', '成品发货', '成品已完成，准备发货', NULL, NULL, NULL, NULL),
(202512180099, 'sign', '用户签收', '用户已收到成品', NULL, NULL, NULL, NULL);

-- 订单 202512100088 的溯源节点（已完成）
INSERT INTO `tms_trace_node` (`order_id`, `node_type`, `title`, `description`, `operator_name`, `environment_data`, `media_url`, `action_time`) VALUES
(202512100088, 'pack', '采发包寄出', '用户头发采发包已寄出，快递单号：SF0987654321', '系统', '{"temp": 21, "hum": 46}', 'https://images.unsplash.com/photo-1605367332306-056158223dc7?q=80&w=200', '2025-12-10 15:20:00'),
(202512100088, 'receive', '工坊签收', '工坊已收到用户头发采发包，开始准备制作', '王师傅', '{"temp": 22, "hum": 47}', 'https://images.unsplash.com/photo-1531238886364-7541f6087593?q=80&w=200', '2025-12-12 10:00:00'),
(202512100088, 'making', '绣制中', '非遗传承人王绣娘正在进行核心图案"牡丹"的绣制，使用了传统的"套针"技法', '王绣娘', '{"temp": 23, "hum": 49}', 'https://images.unsplash.com/photo-1544967082-d9d37d879482?q=80&w=200', '2025-12-14 09:30:00'),
(202512100088, 'ship', '成品发货', '成品已完成，已发货，快递单号：SF1122334455', '赵师傅', '{"temp": 22, "hum": 46}', 'https://images.unsplash.com/photo-1605367332306-056158223dc7?q=80&w=200', '2025-12-16 16:45:00'),
(202512100088, 'sign', '用户签收', '用户已收到成品，确认满意', '系统', '{"temp": 21, "hum": 45}', NULL, '2025-12-18 14:20:00');

-- 订单 202511250077 的溯源节点（已完成）
INSERT INTO `tms_trace_node` (`order_id`, `node_type`, `title`, `description`, `operator_name`, `environment_data`, `media_url`, `action_time`) VALUES
(202511250077, 'pack', '采发包寄出', '用户头发采发包已寄出，快递单号：SF5566778899', '系统', '{"temp": 20, "hum": 47}', 'https://images.unsplash.com/photo-1605367332306-056158223dc7?q=80&w=200', '2025-11-25 09:45:00'),
(202511250077, 'receive', '工坊签收', '工坊已收到用户头发采发包，开始准备制作', '张师傅', '{"temp": 21, "hum": 48}', 'https://images.unsplash.com/photo-1531238886364-7541f6087593?q=80&w=200', '2025-11-27 09:00:00'),
(202511250077, 'making', '绣制中', '非遗传承人张绣娘正在进行核心图案"龙凤呈祥"的绣制，使用了传统的"乱针"技法', '张绣娘', '{"temp": 22, "hum": 50}', 'https://images.unsplash.com/photo-1544967082-d9d37d879482?q=80&w=200', '2025-11-29 10:30:00'),
(202511250077, 'ship', '成品发货', '成品已完成，已发货，快递单号：SF9988776655', '刘师傅', '{"temp": 21, "hum": 46}', 'https://images.unsplash.com/photo-1605367332306-056158223dc7?q=80&w=200', '2025-12-02 15:45:00'),
(202511250077, 'sign', '用户签收', '用户已收到成品，确认满意', '系统', '{"temp": 20, "hum": 45}', NULL, '2025-12-04 16:20:00');

-- 5. 为了前端显示需要，添加一些额外字段到订单表的查询视图
-- 注意：实际项目中可能需要创建视图或修改前端代码来处理字段映射

-- 验证数据插入结果
SELECT '订单数据' AS type, COUNT(*) AS count FROM oms_order WHERE user_id = @user_id;
SELECT '溯源节点数据' AS type, COUNT(*) AS count FROM tms_trace_node WHERE order_id IN (202512180099, 202512100088, 202511250077);

-- 显示插入的订单数据
SELECT id, user_id, product_id, status, total_amount, create_time FROM oms_order WHERE user_id = @user_id;

-- 显示插入的溯源节点数据
SELECT order_id, node_type, title, action_time FROM tms_trace_node WHERE order_id IN (202512180099, 202512100088, 202511250077) ORDER BY order_id, action_time;
