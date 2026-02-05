package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("lms_pack")
public class LogisticsPack {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private String orderNo;
    private Long userId;
    private String userNickname;
    private String sku;
    private Integer quantity;
    private String status;
    private String logisticsNo;
    private LocalDateTime expectedFinishTime;
    private String remark;
    private String mediaUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public LocalDateTime getExpectedFinishTime() { return expectedFinishTime; }
    public void setExpectedFinishTime(LocalDateTime expectedFinishTime) { this.expectedFinishTime = expectedFinishTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public String getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(String mediaUrls) { this.mediaUrls = mediaUrls; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
