package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName("oms_order")
public class Order {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String orderNo;
    private BigDecimal totalAmount;
    private String status;
    private String c2mStatus;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String shippingAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String hairTrackingNo;
    private String finishTrackingNo;

    @TableField(exist = false)
    private List<OrderItem> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getC2mStatus() { return c2mStatus; }
    public void setC2mStatus(String c2mStatus) { this.c2mStatus = c2mStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getHairTrackingNo() { return hairTrackingNo; }
    public void setHairTrackingNo(String hairTrackingNo) { this.hairTrackingNo = hairTrackingNo; }
    public String getFinishTrackingNo() { return finishTrackingNo; }
    public void setFinishTrackingNo(String finishTrackingNo) { this.finishTrackingNo = finishTrackingNo; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
