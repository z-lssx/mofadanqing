package com.mofadanqing.dto;

import lombok.Data;

@Data
public class ConfirmDesignRequest {
    private String taskId;
    private Long orderItemId;
    private String generatedImage;
    private Integer hairAmount;
    private Integer silkSaturation;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getGeneratedImage() {
        return generatedImage;
    }

    public void setGeneratedImage(String generatedImage) {
        this.generatedImage = generatedImage;
    }
}
