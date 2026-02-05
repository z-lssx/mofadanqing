package com.mofadanqing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Order;
import com.mofadanqing.entity.OrderItem;

import java.util.List;

public interface OrderService {
    
    /**
     * 获取订单列表（分页）
     */
    IPage<Order> getOrderPage(Page<Order> page, Long userId, String status);
    
    /**
     * 获取订单详情
     */
    Order getOrderById(Long id);
    
    /**
     * 获取订单详情（包含订单项）
     */
    Order getOrderWithItems(Long id);
    
    /**
     * 创建订单
     */
    Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String paymentMethod);
    
    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, String status, String remark);
    
    /**
     * 获取用户订单列表
     */
    IPage<Order> getUserOrders(Page<Order> page, Long userId, String status);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);
    
    /**
     * 更新订单项的 C2M 状态
     */
    void updateOrderItemC2MStatus(Long itemId, String c2mStatusAlias, String operator, String remark);
    
    /**
     * 获取订单项详情
     */
    OrderItem getOrderItemById(Long itemId);
    
    /**
     * 获取订单项列表
     */
    List<OrderItem> getOrderItems(Long orderId);
}