package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("c2m_design")
public class C2mDesign {
    @TableId
    private Long id;
    private Long orderId;
    private String userPrompt;
    private String refImage;
    private String generatedImage;
    private String styleTag;
    
    // Image Resources
    private String layer1Url;
    private String layer2Url;
    
    // BOM Info
    private java.math.BigDecimal bomHairLength;
    private java.math.BigDecimal bomSilkWeight;
    private Integer estimatedDuration;
    
    // Pricing Info
    private java.math.BigDecimal depositAmount;
    private java.math.BigDecimal finalPrice;
    private java.math.BigDecimal balanceDue;
    
    private LocalDateTime confirmTime;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getRefImage() {
        return refImage;
    }

    public void setRefImage(String refImage) {
        this.refImage = refImage;
    }

    public String getGeneratedImage() {
        return generatedImage;
    }

    public void setGeneratedImage(String generatedImage) {
        this.generatedImage = generatedImage;
    }

    public String getStyleTag() {
        return styleTag;
    }

    public void setStyleTag(String styleTag) {
        this.styleTag = styleTag;
    }

    public String getLayer1Url() {
        return layer1Url;
    }

    public void setLayer1Url(String layer1Url) {
        this.layer1Url = layer1Url;
    }

    public String getLayer2Url() {
        return layer2Url;
    }

    public void setLayer2Url(String layer2Url) {
        this.layer2Url = layer2Url;
    }

    public java.math.BigDecimal getBomHairLength() {
        return bomHairLength;
    }

    public void setBomHairLength(java.math.BigDecimal bomHairLength) {
        this.bomHairLength = bomHairLength;
    }

    public java.math.BigDecimal getBomSilkWeight() {
        return bomSilkWeight;
    }

    public void setBomSilkWeight(java.math.BigDecimal bomSilkWeight) {
        this.bomSilkWeight = bomSilkWeight;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public java.math.BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(java.math.BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public java.math.BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(java.math.BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public java.math.BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(java.math.BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
