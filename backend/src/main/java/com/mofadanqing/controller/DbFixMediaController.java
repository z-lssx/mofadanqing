package com.mofadanqing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class DbFixMediaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/fix-db-media")
    public String fixDbMedia() {
        String[] sqls = {
            "ALTER TABLE oms_order_item ADD COLUMN media_pack TEXT COMMENT '采发包媒体'",
            "ALTER TABLE oms_order_item ADD COLUMN media_workshop TEXT COMMENT '工坊签收媒体'",
            "ALTER TABLE oms_order_item ADD COLUMN media_production TEXT COMMENT '绣制过程媒体'",
            "ALTER TABLE oms_order_item ADD COLUMN media_shipment TEXT COMMENT '成品发货媒体'",
            
            "ALTER TABLE lms_pack ADD COLUMN media_urls TEXT COMMENT '媒体链接'",
            "ALTER TABLE lms_workshop ADD COLUMN media_urls TEXT COMMENT '媒体链接'",
            "ALTER TABLE lms_production ADD COLUMN media_urls TEXT COMMENT '媒体链接'",
            "ALTER TABLE lms_shipment ADD COLUMN media_urls TEXT COMMENT '媒体链接'"
        };

        StringBuilder result = new StringBuilder("DB Media Fix Results:\n");
        for (String sql : sqls) {
            try {
                jdbcTemplate.execute(sql);
                result.append("SUCCESS: ").append(sql).append("\n");
            } catch (Exception e) {
                result.append("FAILED (May already exist): ").append(sql).append(" Error: ").append(e.getMessage()).append("\n");
            }
        }
        
        return result.toString();
    }
}
