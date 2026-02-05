package com.mofadanqing.controller;

import lombok.Data;

@Data
public class CreateC2mOrderRequest {
    private String taskId;
    private Long productId; // Optional: user selects a base product
    private String productName;
    private String address;
    private Integer quantity;
    // ... other order fields

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
