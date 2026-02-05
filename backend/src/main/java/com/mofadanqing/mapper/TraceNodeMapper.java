package com.mofadanqing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mofadanqing.entity.TraceNode;

import java.util.List;

public interface TraceNodeMapper extends BaseMapper<TraceNode> {
    List<TraceNode> selectByOrderId(Long orderId);
}
