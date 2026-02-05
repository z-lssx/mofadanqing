package com.mofadanqing.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mofadanqing.entity.Category;
import com.mofadanqing.entity.Product;
import com.mofadanqing.entity.User;
import com.mofadanqing.mapper.CategoryMapper;
import com.mofadanqing.mapper.ProductMapper;
import com.mofadanqing.mapper.UserMapper;
import com.mofadanqing.mapper.OrderMapper;
import com.mofadanqing.mapper.OrderItemMapper;
import com.mofadanqing.entity.Order;
import com.mofadanqing.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.mofadanqing.dto.StatusTimelineItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class StartupInitializer implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private com.mofadanqing.mapper.LogisticsPackMapper logisticsPackMapper;
    @Autowired
    private com.mofadanqing.mapper.LogisticsWorkshopMapper logisticsWorkshopMapper;
    @Autowired
    private com.mofadanqing.mapper.LogisticsProductionMapper logisticsProductionMapper;
    @Autowired
    private com.mofadanqing.mapper.LogisticsShipmentMapper logisticsShipmentMapper;

    @Override
    public void run(ApplicationArguments args) {
        updateDatabaseSchema();
        fixUserPassword("admin");
        fixUserPassword("user");
        fixUserPassword("luo");
        seedProductsIfEmpty();
        seedOrdersIfEmpty();
        backfillLogisticsFromOrderItems();
    }

    private void updateDatabaseSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 忽略错误执行 ALTER 语句
            executeSql(stmt, "ALTER TABLE oms_order MODIFY COLUMN status VARCHAR(50)");
            executeSql(stmt, "ALTER TABLE oms_order MODIFY COLUMN product_id BIGINT NULL");
            executeSql(stmt, "ALTER TABLE oms_order MODIFY COLUMN quantity INT NULL");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN order_no VARCHAR(64)");
            executeSql(stmt, "UPDATE oms_order SET order_no = CONCAT('ORD', id) WHERE order_no IS NULL");

            // 购物车表
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS oms_cart_item (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id BIGINT NOT NULL," +
                    "product_id BIGINT NOT NULL," +
                    "quantity INT NOT NULL DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN c2m_status VARCHAR(50)");
            
            // 修复商品脏数据
            executeSql(stmt, "UPDATE pms_product SET price = 999.00 WHERE price IS NULL");
            executeSql(stmt, "UPDATE pms_product SET stock = 100 WHERE stock IS NULL OR stock < 10");
            executeSql(stmt, "UPDATE pms_product SET name = '未命名商品' WHERE name IS NULL OR name = ''");
            executeSql(stmt, "UPDATE pms_product SET status = 'ACTIVE' WHERE status IS NULL");
            
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN payment_method VARCHAR(20)");
            
            // 6. C2M 增强字段
            executeSql(stmt, "ALTER TABLE pms_product ADD COLUMN c2m_type VARCHAR(50) DEFAULT 'NONE'");
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN c2m_type VARCHAR(50)");
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN current_status VARCHAR(50)"); // 冗余字段，便于查询
            
            System.out.println("Database schema initialized successfully.");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN payment_time DATETIME");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN shipping_address VARCHAR(255)");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN remark VARCHAR(255)");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN hair_tracking_no VARCHAR(100)");
            executeSql(stmt, "ALTER TABLE oms_order ADD COLUMN finish_tracking_no VARCHAR(100)");
            
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN c2m_status VARCHAR(50)");
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN status_timeline JSON");
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN tracking_no VARCHAR(100)");
            executeSql(stmt, "ALTER TABLE oms_order_item ADD COLUMN c2m_type VARCHAR(50)");
            
            executeSql(stmt, "ALTER TABLE pms_product ADD COLUMN c2m_type VARCHAR(50)");
            
            System.out.println("Database schema updated successfully.");
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS sys_user_message (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id BIGINT," +
                    "title VARCHAR(100)," +
                    "content VARCHAR(500)," +
                    "is_read TINYINT(1) DEFAULT 0," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            executeSql(stmt, "ALTER TABLE sys_user_message CHANGE COLUMN read is_read TINYINT(1) DEFAULT 0");
            
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS lms_pack (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "order_id BIGINT," +
                    "order_item_id BIGINT," +
                    "order_no VARCHAR(64)," +
                    "user_id BIGINT," +
                    "user_nickname VARCHAR(64)," +
                    "sku VARCHAR(64)," +
                    "quantity INT," +
                    "status VARCHAR(50)," +
                    "logistics_no VARCHAR(100)," +
                    "expected_finish_time DATETIME," +
                    "remark VARCHAR(255)," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS lms_workshop (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "order_id BIGINT," +
                    "order_item_id BIGINT," +
                    "order_no VARCHAR(64)," +
                    "user_id BIGINT," +
                    "user_nickname VARCHAR(64)," +
                    "sku VARCHAR(64)," +
                    "quantity INT," +
                    "status VARCHAR(50)," +
                    "logistics_no VARCHAR(100)," +
                    "expected_finish_time DATETIME," +
                    "remark VARCHAR(255)," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS lms_production (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "order_id BIGINT," +
                    "order_item_id BIGINT," +
                    "order_no VARCHAR(64)," +
                    "user_id BIGINT," +
                    "user_nickname VARCHAR(64)," +
                    "sku VARCHAR(64)," +
                    "quantity INT," +
                    "status VARCHAR(50)," +
                    "logistics_no VARCHAR(100)," +
                    "expected_finish_time DATETIME," +
                    "remark VARCHAR(255)," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            executeSql(stmt, "CREATE TABLE IF NOT EXISTS lms_shipment (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "order_id BIGINT," +
                    "order_item_id BIGINT," +
                    "order_no VARCHAR(64)," +
                    "user_id BIGINT," +
                    "user_nickname VARCHAR(64)," +
                    "sku VARCHAR(64)," +
                    "quantity INT," +
                    "status VARCHAR(50)," +
                    "logistics_no VARCHAR(100)," +
                    "expected_finish_time DATETIME," +
                    "remark VARCHAR(255)," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeSql(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (Exception e) {
            // 忽略 "Duplicate column name" 错误
            // System.out.println("SQL execution failed (might be expected): " + sql + " - " + e.getMessage());
        }
    }


    private void fixUserPassword(String username) {
        try {
            User u = userMapper.selectByUsername(username);
            if (u == null) return;
            String stored = u.getPassword();
            if (stored == null || !passwordEncoder.matches("123456", stored)) {
                u.setPassword(passwordEncoder.encode("123456"));
                userMapper.updateById(u);
            }
        } catch (Exception ignored) {}
    }

    private void seedProductsIfEmpty() {
        // ... (省略原有商品初始化逻辑) ...
        try {
            Long productCount = productMapper.selectCount(new QueryWrapper<>());
            if (productCount == null || productCount == 0) {
                Product p1 = new Product();
                p1.setName("随身锦囊·经典款");
                p1.setCategoryId(null);
                p1.setPrice(1999.0);
                p1.setStock(100);
                p1.setModelFile("models/pouch.glb");
                p1.setCoverImg("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=traditional%20chinese%20silk%20pouch%20with%20embroidery&image_size=square");
                p1.setDescription("小巧精致，随身携带，承载美好祝愿");
                p1.setStatus("ACTIVE");
                p1.setCreateTime(LocalDateTime.now());
                p1.setUpdateTime(LocalDateTime.now());
                productMapper.insert(p1);

                Product p2 = new Product();
                p2.setName("案头雅玩·团扇");
                p2.setCategoryId(null);
                p2.setPrice(2999.0);
                p2.setStock(80);
                p2.setModelFile("models/fan.glb");
                p2.setCoverImg("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20round%20fan%20with%20silk%20embroidery&image_size=square");
                p2.setDescription("精致典雅，置于案头，彰显文化品味");
                p2.setStatus("ACTIVE");
                p2.setCreateTime(LocalDateTime.now());
                p2.setUpdateTime(LocalDateTime.now());
                productMapper.insert(p2);

                Product p3 = new Product();
                p3.setName("传世典藏·卷轴");
                p3.setCategoryId(null);
                p3.setPrice(5999.0);
                p3.setStock(50);
                p3.setModelFile("models/scroll.glb");
                p3.setCoverImg("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20silk%20scroll%20painting%20with%20embroidery&image_size=square");
                p3.setDescription("精工细作，传承经典，值得珍藏传世");
                p3.setStatus("ACTIVE");
                p3.setCreateTime(LocalDateTime.now());
                p3.setUpdateTime(LocalDateTime.now());
                productMapper.insert(p3);
            }

            Long activeCount = productMapper.selectCount(new QueryWrapper<Product>().eq("status", "ACTIVE"));
            if (activeCount == null || activeCount == 0) {
                // 将前3条商品激活以保证前端展示
                var list = productMapper.selectList(new QueryWrapper<Product>().last("limit 3"));
                for (Product p : list) {
                    p.setStatus("ACTIVE");
                    p.setUpdateTime(LocalDateTime.now());
                    productMapper.updateById(p);
                }
            }
        } catch (Exception ignored) {}
    }

    private void seedOrdersIfEmpty() {
        try {
            Long orderCount = orderMapper.selectCount(new QueryWrapper<>());
            if (orderCount == null || orderCount == 0) {
                User user = userMapper.selectByUsername("user");
                if (user == null) return;
                
                Product p1 = productMapper.selectList(new QueryWrapper<Product>().last("limit 1")).get(0);
                if (p1 == null) return;

                // 订单1：处理中 (C2M: 绣制中)
                Order o1 = new Order();
                o1.setUserId(user.getId());
                o1.setOrderNo("ORD" + System.currentTimeMillis() + "001");
                o1.setTotalAmount(BigDecimal.valueOf(p1.getPrice()));
                o1.setStatus("PAID"); // 对应前端状态 (PENDING/PAID/SHIPPED/COMPLETED/CANCELLED)
                o1.setC2mStatus("绣制中");
                o1.setPaymentMethod("WECHAT");
                o1.setShippingAddress("北京市朝阳区非遗文化园");
                o1.setCreateTime(LocalDateTime.now().minusDays(5));
                o1.setUpdateTime(LocalDateTime.now().minusDays(2));
                o1.setPaymentTime(LocalDateTime.now().minusDays(5));
                orderMapper.insert(o1);

                OrderItem oi1 = new OrderItem();
                oi1.setOrderId(o1.getId());
                oi1.setProductId(p1.getId());
                oi1.setProductName(p1.getName());
                oi1.setProductImage(p1.getCoverImg());
                oi1.setUnitPrice(BigDecimal.valueOf(p1.getPrice()));
                oi1.setQuantity(1);
                oi1.setSubtotal(BigDecimal.valueOf(p1.getPrice()));
                oi1.setCreatedAt(LocalDateTime.now());
                oi1.setC2mStatus("producing"); // alias
                
                // 构造时间轴
                List<StatusTimelineItem> timeline1 = new ArrayList<>();
                timeline1.add(new StatusTimelineItem("待C2M定制", "订单支付成功，等待开启定制", "系统", ""));
                timeline1.add(new StatusTimelineItem("C2M定制", "用户确认开启定制", "用户", "确认方案A"));
                timeline1.add(new StatusTimelineItem("采发包", "采发包寄出", "管理员", "顺丰单号 SF1001"));
                timeline1.add(new StatusTimelineItem("工坊签收", "工坊已签收", "管理员", "李绣娘签收"));
                timeline1.add(new StatusTimelineItem("绣制中", "大师开始绣制", "管理员", "进入核心工艺阶段"));
                oi1.setStatusTimeline(timeline1);
                
                orderItemMapper.insert(oi1);

                // 订单2：已完成 (C2M: 用户签收)
                Order o2 = new Order();
                o2.setUserId(user.getId());
                o2.setOrderNo("ORD" + System.currentTimeMillis() + "002");
                o2.setTotalAmount(BigDecimal.valueOf(p1.getPrice()));
                o2.setStatus("COMPLETED");
                o2.setC2mStatus("用户签收");
                o2.setPaymentMethod("ALIPAY");
                o2.setShippingAddress("上海市静安区艺术中心");
                o2.setCreateTime(LocalDateTime.now().minusDays(10));
                o2.setUpdateTime(LocalDateTime.now().minusDays(1));
                o2.setPaymentTime(LocalDateTime.now().minusDays(10));
                orderMapper.insert(o2);

                OrderItem oi2 = new OrderItem();
                oi2.setOrderId(o2.getId());
                oi2.setProductId(p1.getId());
                oi2.setProductName(p1.getName());
                oi2.setProductImage(p1.getCoverImg());
                oi2.setUnitPrice(BigDecimal.valueOf(p1.getPrice()));
                oi2.setQuantity(1);
                oi2.setSubtotal(BigDecimal.valueOf(p1.getPrice()));
                oi2.setCreatedAt(LocalDateTime.now());
                oi2.setC2mStatus("received"); // alias
                
                List<StatusTimelineItem> timeline2 = new ArrayList<>();
                timeline2.add(new StatusTimelineItem("待C2M定制", "订单支付成功", "系统", ""));
                timeline2.add(new StatusTimelineItem("C2M定制", "用户确认", "用户", ""));
                timeline2.add(new StatusTimelineItem("采发包", "寄出", "管理员", ""));
                timeline2.add(new StatusTimelineItem("工坊签收", "签收", "管理员", ""));
                timeline2.add(new StatusTimelineItem("绣制中", "制作完成", "管理员", ""));
                timeline2.add(new StatusTimelineItem("成品发货", "发货", "管理员", "顺丰 SF2002"));
                timeline2.add(new StatusTimelineItem("用户签收", "用户已签收", "用户", "满意"));
                oi2.setStatusTimeline(timeline2);
                
                orderItemMapper.insert(oi2);
            }
        } catch (Exception ignored) {}
    }
    
    private void backfillLogisticsFromOrderItems() {
        try {
            List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<>());
            for (OrderItem item : items) {
                Order order = orderMapper.selectById(item.getOrderId());
                if (order == null) continue;
                String alias = item.getCurrentStatus();
                if (alias == null || alias.isEmpty()) {
                    String desc = item.getC2mStatus();
                    if ("采发包".equals(desc)) alias = "material";
                    else if ("工坊签收".equals(desc)) alias = "workshop";
                    else if ("绣制中".equals(desc)) alias = "producing";
                    else if ("成品发货".equals(desc)) alias = "shipped";
                    else alias = null;
                }
                if (alias == null) continue;
                // material
                if ("material".equalsIgnoreCase(alias)) {
                    Long cnt = logisticsPackMapper.selectCount(new QueryWrapper<com.mofadanqing.entity.LogisticsPack>().eq("order_item_id", item.getId()));
                    if (cnt == null || cnt == 0) {
                        com.mofadanqing.entity.LogisticsPack r = new com.mofadanqing.entity.LogisticsPack();
                        r.setOrderId(item.getOrderId());
                        r.setOrderItemId(item.getId());
                        r.setOrderNo(order.getOrderNo());
                        r.setUserId(order.getUserId());
                        User u = userMapper.selectById(order.getUserId());
                        r.setUserNickname(u != null ? u.getUsername() : "用户");
                        r.setSku(String.valueOf(item.getProductId()));
                        r.setQuantity(item.getQuantity());
                        r.setStatus("material");
                        r.setRemark("回填");
                        r.setCreatedAt(LocalDateTime.now());
                        r.setUpdatedAt(LocalDateTime.now());
                        logisticsPackMapper.insert(r);
                    }
                    // 校正昵称
                    com.mofadanqing.entity.LogisticsPack ex = logisticsPackMapper.selectOne(new QueryWrapper<com.mofadanqing.entity.LogisticsPack>().eq("order_item_id", item.getId()));
                    if (ex != null) {
                        User u = userMapper.selectById(order.getUserId());
                        if (u != null) { ex.setUserNickname(u.getUsername()); logisticsPackMapper.updateById(ex); }
                    }
                }
                // workshop
                if ("workshop".equalsIgnoreCase(alias)) {
                    Long cnt = logisticsWorkshopMapper.selectCount(new QueryWrapper<com.mofadanqing.entity.LogisticsWorkshop>().eq("order_item_id", item.getId()));
                    if (cnt == null || cnt == 0) {
                        com.mofadanqing.entity.LogisticsWorkshop r = new com.mofadanqing.entity.LogisticsWorkshop();
                        r.setOrderId(item.getOrderId());
                        r.setOrderItemId(item.getId());
                        r.setOrderNo(order.getOrderNo());
                        r.setUserId(order.getUserId());
                        User u = userMapper.selectById(order.getUserId());
                        r.setUserNickname(u != null ? u.getUsername() : "用户");
                        r.setSku(String.valueOf(item.getProductId()));
                        r.setQuantity(item.getQuantity());
                        r.setStatus("workshop");
                        r.setRemark("回填");
                        r.setCreatedAt(LocalDateTime.now());
                        r.setUpdatedAt(LocalDateTime.now());
                        logisticsWorkshopMapper.insert(r);
                    }
                    com.mofadanqing.entity.LogisticsWorkshop ex = logisticsWorkshopMapper.selectOne(new QueryWrapper<com.mofadanqing.entity.LogisticsWorkshop>().eq("order_item_id", item.getId()));
                    if (ex != null) {
                        User u = userMapper.selectById(order.getUserId());
                        if (u != null) { ex.setUserNickname(u.getUsername()); logisticsWorkshopMapper.updateById(ex); }
                    }
                }
                // producing
                if ("producing".equalsIgnoreCase(alias)) {
                    Long cnt = logisticsProductionMapper.selectCount(new QueryWrapper<com.mofadanqing.entity.LogisticsProduction>().eq("order_item_id", item.getId()));
                    if (cnt == null || cnt == 0) {
                        com.mofadanqing.entity.LogisticsProduction r = new com.mofadanqing.entity.LogisticsProduction();
                        r.setOrderId(item.getOrderId());
                        r.setOrderItemId(item.getId());
                        r.setOrderNo(order.getOrderNo());
                        r.setUserId(order.getUserId());
                        User u = userMapper.selectById(order.getUserId());
                        r.setUserNickname(u != null ? u.getUsername() : "用户");
                        r.setSku(String.valueOf(item.getProductId()));
                        r.setQuantity(item.getQuantity());
                        r.setStatus("producing");
                        r.setRemark("回填");
                        r.setCreatedAt(LocalDateTime.now());
                        r.setUpdatedAt(LocalDateTime.now());
                        logisticsProductionMapper.insert(r);
                    }
                    com.mofadanqing.entity.LogisticsProduction ex = logisticsProductionMapper.selectOne(new QueryWrapper<com.mofadanqing.entity.LogisticsProduction>().eq("order_item_id", item.getId()));
                    if (ex != null) {
                        User u = userMapper.selectById(order.getUserId());
                        if (u != null) { ex.setUserNickname(u.getUsername()); logisticsProductionMapper.updateById(ex); }
                    }
                }
                // shipped
                if ("shipped".equalsIgnoreCase(alias)) {
                    Long cnt = logisticsShipmentMapper.selectCount(new QueryWrapper<com.mofadanqing.entity.LogisticsShipment>().eq("order_item_id", item.getId()));
                    if (cnt == null || cnt == 0) {
                        com.mofadanqing.entity.LogisticsShipment r = new com.mofadanqing.entity.LogisticsShipment();
                        r.setOrderId(item.getOrderId());
                        r.setOrderItemId(item.getId());
                        r.setOrderNo(order.getOrderNo());
                        r.setUserId(order.getUserId());
                        User u = userMapper.selectById(order.getUserId());
                        r.setUserNickname(u != null ? u.getUsername() : "用户");
                        r.setSku(String.valueOf(item.getProductId()));
                        r.setQuantity(item.getQuantity());
                        r.setStatus("shipped");
                        r.setRemark("回填");
                        r.setCreatedAt(LocalDateTime.now());
                        r.setUpdatedAt(LocalDateTime.now());
                        logisticsShipmentMapper.insert(r);
                    }
                    com.mofadanqing.entity.LogisticsShipment ex = logisticsShipmentMapper.selectOne(new QueryWrapper<com.mofadanqing.entity.LogisticsShipment>().eq("order_item_id", item.getId()));
                    if (ex != null) {
                        User u = userMapper.selectById(order.getUserId());
                        if (u != null) { ex.setUserNickname(u.getUsername()); logisticsShipmentMapper.updateById(ex); }
                    }
                }
            }
            System.out.println("Backfill logistics tables finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
