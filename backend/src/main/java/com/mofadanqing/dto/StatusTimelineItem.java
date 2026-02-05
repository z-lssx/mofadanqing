package com.mofadanqing.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatusTimelineItem {
    private String status;
    private String description;
    private String time;
    private String operator;
    private String remark;

    public StatusTimelineItem() {}

    public StatusTimelineItem(String status, String description, String operator, String remark) {
        this.status = status;
        this.description = description;
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.operator = operator;
        this.remark = remark;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
