package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "ai_task", autoResultMap = true)
public class AiTask {
    @TableId
    private String taskId;
    private String prompt;
    private String refImg;
    private String style;
    private String status; // PENDING, COMPLETED, FAILED
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    
    @TableField(exist = false)
    private String layer1Url; // Skeleton/Hair Layer
    @TableField(exist = false)
    private String layer2Url; // Polishing/Silk Layer
    
    @TableField(exist = false)
    private BigDecimal hairLength;
    @TableField(exist = false)
    private BigDecimal silkWeight;
    @TableField(exist = false)
    private BigDecimal estimatedCost;
    
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Manual Getters/Setters to ensure Lombok is not the issue or if Lombok processing is failing in this environment
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getRefImg() { return refImg; }
    public void setRefImg(String refImg) { this.refImg = refImg; }
    
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    
    public String getLayer1Url() { return layer1Url; }
    public void setLayer1Url(String layer1Url) { this.layer1Url = layer1Url; }
    
    public String getLayer2Url() { return layer2Url; }
    public void setLayer2Url(String layer2Url) { this.layer2Url = layer2Url; }
    
    public BigDecimal getHairLength() { return hairLength; }
    public void setHairLength(BigDecimal hairLength) { this.hairLength = hairLength; }
    
    public BigDecimal getSilkWeight() { return silkWeight; }
    public void setSilkWeight(BigDecimal silkWeight) { this.silkWeight = silkWeight; }
    
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
