package com.mofadanqing.service.impl;

import com.mofadanqing.entity.TraceNode;
import com.mofadanqing.mapper.TraceNodeMapper;
import com.mofadanqing.service.TraceNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TraceNodeServiceImpl implements TraceNodeService {

    @Autowired
    private TraceNodeMapper traceNodeMapper;

    @Override
    public List<TraceNode> getTraceNodesByOrderId(Long orderId) {
        return traceNodeMapper.selectByOrderId(orderId);
    }
}
