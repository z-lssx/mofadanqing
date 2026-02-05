package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mofadanqing.dto.StatusTimelineItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "oms_order_item", autoResultMap = true)
public class OrderItem {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    private String c2mStatus;
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<StatusTimelineItem> statusTimeline;
    
    private String trackingNo;
    private String c2mType;
    private String currentStatus;
    
    // Media URLs for Trace
    private String mediaPack;
    private String mediaWorkshop;
    private String mediaProduction;
    private String mediaShipment;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getC2mStatus() {
        return c2mStatus;
    }

    public void setC2mStatus(String c2mStatus) {
        this.c2mStatus = c2mStatus;
    }

    public List<StatusTimelineItem> getStatusTimeline() {
        return statusTimeline;
    }

    public void setStatusTimeline(List<StatusTimelineItem> statusTimeline) {
        this.statusTimeline = statusTimeline;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getC2mType() {
        return c2mType;
    }

    public void setC2mType(String c2mType) {
        this.c2mType = c2mType;
    }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getMediaPack() { return mediaPack; }
    public void setMediaPack(String mediaPack) { this.mediaPack = mediaPack; }

    public String getMediaWorkshop() { return mediaWorkshop; }
    public void setMediaWorkshop(String mediaWorkshop) { this.mediaWorkshop = mediaWorkshop; }

    public String getMediaProduction() { return mediaProduction; }
    public void setMediaProduction(String mediaProduction) { this.mediaProduction = mediaProduction; }

    public String getMediaShipment() { return mediaShipment; }
    public void setMediaShipment(String mediaShipment) { this.mediaShipment = mediaShipment; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
