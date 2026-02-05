package com.mofadanqing.controller;

import com.mofadanqing.entity.TraceNode;
import com.mofadanqing.service.TraceNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/trace")
public class TraceController {

    @Autowired
    private TraceNodeService traceNodeService;

    /**
     * 根据订单ID获取溯源节点列表
     * @param orderId 订单ID
     * @return 溯源节点列表
     */
    @GetMapping("/{orderId}")
    public List<TraceNode> getTraceByOrderId(@PathVariable Long orderId) {
        return traceNodeService.getTraceNodesByOrderId(orderId);
    }
}
