package com.mofadanqing.service;

import com.mofadanqing.entity.TraceNode;
import java.util.List;

public interface TraceNodeService {
    /**
     * 根据订单ID获取溯源节点列表
     * @param orderId 订单ID
     * @return 溯源节点列表
     */
    List<TraceNode> getTraceNodesByOrderId(Long orderId);
}
