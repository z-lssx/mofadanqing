package com.mofadanqing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking database schema for sys_user...");
        
        try {
            // 检查 nickname 列是否存在
            checkAndAddColumn("sys_user", "nickname", "VARCHAR(64) DEFAULT NULL");
            // 检查 openid 列是否存在
            checkAndAddColumn("sys_user", "openid", "VARCHAR(64) DEFAULT NULL");
            
            // 修复 email 列长度和允许 NULL
            System.out.println("Modifying column 'email' to VARCHAR(255) NULL...");
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user MODIFY COLUMN email VARCHAR(255) NULL");
            } catch (Exception e) {
                System.err.println("Failed to modify email column: " + e.getMessage());
            }

            // --- 订单相关字段迁移 ---
            System.out.println("Checking database schema for oms_order...");
            checkAndAddColumn("oms_order", "c2m_status", "VARCHAR(50) DEFAULT NULL");

            System.out.println("Checking database schema for oms_order_item...");
            checkAndAddColumn("oms_order_item", "c2m_status", "VARCHAR(50) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "status_timeline", "JSON DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "tracking_no", "VARCHAR(100) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "c2m_type", "VARCHAR(50) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "current_status", "VARCHAR(50) DEFAULT NULL");
            // 媒体字段
            checkAndAddColumn("oms_order_item", "media_pack", "VARCHAR(500) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "media_workshop", "VARCHAR(500) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "media_production", "VARCHAR(500) DEFAULT NULL");
            checkAndAddColumn("oms_order_item", "media_shipment", "VARCHAR(500) DEFAULT NULL");

            // --- 商品相关字段迁移 ---
            System.out.println("Checking database schema for pms_product...");
            checkAndAddColumn("pms_product", "c2m_type", "VARCHAR(50) DEFAULT 'NONE'");

            System.out.println("Database migration completed.");

        } catch (Exception e) {
            System.err.println("Database migration failed: " + e.getMessage());
        }
    }

    private void checkAndAddColumn(String tableName, String columnName, String columnDefinition) {
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList("SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'");
            if (columns.isEmpty()) {
                System.out.println("Adding column '" + columnName + "' to " + tableName + "...");
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
            } else {
                System.out.println("Modifying column '" + columnName + "' in " + tableName + " to ensure type...");
                jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + columnDefinition);
            }
        } catch (Exception e) {
            System.err.println("Failed to add/modify column " + columnName + " to " + tableName + ": " + e.getMessage());
        }
    }
}
