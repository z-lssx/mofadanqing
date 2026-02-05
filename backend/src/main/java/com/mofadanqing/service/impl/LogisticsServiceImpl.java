package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mofadanqing.dto.StatusTimelineItem;
import com.mofadanqing.entity.*;
import com.mofadanqing.enums.C2MStatus;
import com.mofadanqing.mapper.*;
import com.mofadanqing.service.LogisticsService;
import com.mofadanqing.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogisticsServiceImpl implements LogisticsService {
    @Autowired private LogisticsPackMapper packMapper;
    @Autowired private LogisticsWorkshopMapper workshopMapper;
    @Autowired private LogisticsProductionMapper productionMapper;
    @Autowired private LogisticsShipmentMapper shipmentMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private com.mofadanqing.mapper.UserMapper userMapper;
    @Autowired private OperationLogService operationLogService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private com.mofadanqing.service.UserMessageService userMessageService;

    @Override
    public IPage<LogisticsPack> listPack(Page<LogisticsPack> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime) {
        QueryWrapper<LogisticsPack> w = new QueryWrapper<>();
        if (userId != null) w.eq("user_id", userId);
        if (nickname != null && !nickname.isEmpty()) w.like("user_nickname", nickname);
        if (orderNo != null) {
            String on = orderNo.trim();
            if (!on.isEmpty()) w.like("order_no", on);
        }
        if (status != null) {
            String s = statusAlias(status);
            if (!s.isEmpty()) w.apply("LOWER(status) = {0}", s.toLowerCase());
        }
        if (startTime != null || endTime != null) {
            LocalDateTime st = parseDateTime(startTime, false);
            LocalDateTime et = parseDateTime(endTime, true);
            if (st != null && et != null) w.between("updated_at", st, et);
            else if (st != null) w.ge("updated_at", st);
            else if (et != null) w.le("updated_at", et);
        }
        w.orderByDesc("updated_at");
        return packMapper.selectPage(page, w);
    }

    @Override
    public IPage<LogisticsWorkshop> listWorkshop(Page<LogisticsWorkshop> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime) {
        QueryWrapper<LogisticsWorkshop> w = new QueryWrapper<>();
        if (userId != null) w.eq("user_id", userId);
        if (nickname != null && !nickname.isEmpty()) w.like("user_nickname", nickname);
        if (orderNo != null) {
            String on = orderNo.trim();
            if (!on.isEmpty()) w.like("order_no", on);
        }
        if (status != null) {
            String s = statusAlias(status);
            if (!s.isEmpty()) w.apply("LOWER(status) = {0}", s.toLowerCase());
        }
        if (startTime != null || endTime != null) {
            LocalDateTime st = parseDateTime(startTime, false);
            LocalDateTime et = parseDateTime(endTime, true);
            if (st != null && et != null) w.between("updated_at", st, et);
            else if (st != null) w.ge("updated_at", st);
            else if (et != null) w.le("updated_at", et);
        }
        w.orderByDesc("updated_at");
        return workshopMapper.selectPage(page, w);
    }

    @Override
    public IPage<LogisticsProduction> listProduction(Page<LogisticsProduction> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime) {
        QueryWrapper<LogisticsProduction> w = new QueryWrapper<>();
        if (userId != null) w.eq("user_id", userId);
        if (nickname != null && !nickname.isEmpty()) w.like("user_nickname", nickname);
        if (orderNo != null) {
            String on = orderNo.trim();
            if (!on.isEmpty()) w.like("order_no", on);
        }
        if (status != null) {
            String s = statusAlias(status);
            if (!s.isEmpty()) w.apply("LOWER(status) = {0}", s.toLowerCase());
        }
        if (startTime != null || endTime != null) {
            LocalDateTime st = parseDateTime(startTime, false);
            LocalDateTime et = parseDateTime(endTime, true);
            if (st != null && et != null) w.between("updated_at", st, et);
            else if (st != null) w.ge("updated_at", st);
            else if (et != null) w.le("updated_at", et);
        }
        w.orderByDesc("updated_at");
        return productionMapper.selectPage(page, w);
    }

    @Override
    public IPage<LogisticsShipment> listShipment(Page<LogisticsShipment> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime) {
        QueryWrapper<LogisticsShipment> w = new QueryWrapper<>();
        if (userId != null) w.eq("user_id", userId);
        if (nickname != null && !nickname.isEmpty()) w.like("user_nickname", nickname);
        if (orderNo != null) {
            String on = orderNo.trim();
            if (!on.isEmpty()) w.like("order_no", on);
        }
        if (status != null) {
            String s = statusAlias(status);
            if (!s.isEmpty()) w.apply("LOWER(status) = {0}", s.toLowerCase());
        }
        if (startTime != null || endTime != null) {
            LocalDateTime st = parseDateTime(startTime, false);
            LocalDateTime et = parseDateTime(endTime, true);
            if (st != null && et != null) w.between("updated_at", st, et);
            else if (st != null) w.ge("updated_at", st);
            else if (et != null) w.le("updated_at", et);
        }
        w.orderByDesc("updated_at");
        return shipmentMapper.selectPage(page, w);
    }

    private String statusAlias(String s) {
        String v = s.trim();
        if (v.isEmpty()) return "";
        if ("采发包".equals(v)) return "material";
        if ("工坊签收".equals(v)) return "workshop";
        if ("绣制中".equals(v)) return "producing";
        if ("成品发货".equals(v)) return "shipped";
        return v;
    }

    private LocalDateTime parseDateTime(String s, boolean end) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(v, f);
        } catch (Exception ignored) {}
        try {
            DateTimeFormatter f2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime dt = LocalDateTime.of(java.time.LocalDate.parse(v, f2), end ? LocalTime.MAX : LocalTime.MIN);
            return dt;
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void createPack(LogisticsPack pack) {
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("order_no", pack.getOrderNo()));
        if (order != null) {
            pack.setOrderId(order.getId());
            pack.setUserId(order.getUserId());
            com.mofadanqing.entity.User u = userMapper.selectById(order.getUserId());
            if (u != null) pack.setUserNickname(u.getUsername());
            
            // Sync media to OrderItem
            if (pack.getOrderItemId() != null) {
                OrderItem item = orderItemMapper.selectById(pack.getOrderItemId());
                if (item != null) {
                    item.setMediaPack(pack.getMediaUrls());
                    orderItemMapper.updateById(item);
                    
                    // Auto-fill LogisticsNo from OrderItem if missing
                    if (pack.getLogisticsNo() == null || pack.getLogisticsNo().trim().isEmpty()) {
                        pack.setLogisticsNo(item.getTrackingNo());
                    }
                }
            }
        }
        pack.setStatus("material");
        pack.setCreatedAt(LocalDateTime.now());
        pack.setUpdatedAt(LocalDateTime.now());
        packMapper.insert(pack);
    }

    @Override
    public void updatePack(LogisticsPack pack) {
        pack.setUpdatedAt(LocalDateTime.now());
        packMapper.updateById(pack);
        
        // Sync media to OrderItem
        LogisticsPack existing = packMapper.selectById(pack.getId());
        if (existing != null && existing.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(existing.getOrderItemId());
            if (item != null && pack.getMediaUrls() != null) {
                item.setMediaPack(pack.getMediaUrls());
                orderItemMapper.updateById(item);
            }
        }
    }
    @Override
    public void deletePack(Long id) { packMapper.deleteById(id); }

    @Override
    @Transactional
    public void confirmIssue(Long id, Long operatorId, String operatorName, String remark) {
        LogisticsPack pack = packMapper.selectById(id);
        LogisticsWorkshop ws = new LogisticsWorkshop();
        ws.setOrderId(pack.getOrderId());
        ws.setOrderItemId(pack.getOrderItemId());
        ws.setOrderNo(pack.getOrderNo());
        ws.setUserId(pack.getUserId());
        Order orderForWs = orderMapper.selectById(pack.getOrderId());
        String wsName = pack.getUserNickname();
        if (orderForWs != null) {
            com.mofadanqing.entity.User uu = userMapper.selectById(orderForWs.getUserId());
            if (uu != null) wsName = uu.getUsername();
        }
        ws.setUserNickname(wsName);
        ws.setSku(pack.getSku());
        ws.setQuantity(pack.getQuantity());
        ws.setStatus("workshop");
        ws.setLogisticsNo(pack.getLogisticsNo());
        ws.setExpectedFinishTime(pack.getExpectedFinishTime());
        ws.setRemark(remark);
        ws.setCreatedAt(LocalDateTime.now());
        ws.setUpdatedAt(LocalDateTime.now());
        workshopMapper.insert(ws);
        packMapper.deleteById(id);
        OrderItem item = orderItemMapper.selectById(pack.getOrderItemId());
        // Sync media again just in case
        if (pack.getMediaUrls() != null) {
            item.setMediaPack(pack.getMediaUrls());
        }
        item.setC2mStatus("工坊签收");
        item.setCurrentStatus("workshop");
        List<StatusTimelineItem> timeline = new ArrayList<>();
        try {
            if (item.getStatusTimeline() instanceof List) {
                timeline = (List<StatusTimelineItem>) item.getStatusTimeline();
            }
        } catch (Exception ignored) {}
        timeline.add(new StatusTimelineItem("工坊签收", "工坊签收确认", operatorName, remark));
        item.setStatusTimeline(timeline);
        orderItemMapper.updateById(item);
        operationLogService.logOperation(operatorId, "confirm-issue", "logistics", item.getId(), null, null, null, null);
        Order order = orderMapper.selectById(item.getOrderId());
        userMessageService.addMessage(order.getUserId(), "订单工坊签收", "订单项已签收，开始准备绣制");
    }

    @Override
    public void createWorkshop(LogisticsWorkshop record) { 
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        workshopMapper.insert(record); 
    }
    @Override
    public void updateWorkshop(LogisticsWorkshop record) { 
        record.setUpdatedAt(LocalDateTime.now());
        workshopMapper.updateById(record);
        
        // Sync media
        LogisticsWorkshop existing = workshopMapper.selectById(record.getId());
        if (existing != null && existing.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(existing.getOrderItemId());
            if (item != null && record.getMediaUrls() != null) {
                item.setMediaWorkshop(record.getMediaUrls());
                orderItemMapper.updateById(item);
            }
        }
    }
    @Override
    public void deleteWorkshop(Long id) { workshopMapper.deleteById(id); }

    @Override
    @Transactional
    public void confirmReceive(Long id, Long operatorId, String operatorName, String remark) {
        LogisticsWorkshop ws = workshopMapper.selectById(id);
        LogisticsProduction pr = new LogisticsProduction();
        pr.setOrderId(ws.getOrderId());
        pr.setOrderItemId(ws.getOrderItemId());
        pr.setOrderNo(ws.getOrderNo());
        pr.setUserId(ws.getUserId());
        Order orderForPr = orderMapper.selectById(ws.getOrderId());
        String prName = ws.getUserNickname();
        if (orderForPr != null) {
            com.mofadanqing.entity.User uu = userMapper.selectById(orderForPr.getUserId());
            if (uu != null) prName = uu.getUsername();
        }
        pr.setUserNickname(prName);
        pr.setSku(ws.getSku());
        pr.setQuantity(ws.getQuantity());
        pr.setStatus("producing");
        pr.setLogisticsNo(ws.getLogisticsNo());
        pr.setExpectedFinishTime(ws.getExpectedFinishTime());
        pr.setRemark(remark);
        pr.setCreatedAt(LocalDateTime.now());
        pr.setUpdatedAt(LocalDateTime.now());
        productionMapper.insert(pr);
        workshopMapper.deleteById(id);
        OrderItem item = orderItemMapper.selectById(ws.getOrderItemId());
        // Sync media
        if (ws.getMediaUrls() != null) {
            item.setMediaWorkshop(ws.getMediaUrls());
        }
        item.setC2mStatus("绣制中");
        item.setCurrentStatus("producing");
        List<StatusTimelineItem> timeline = new ArrayList<>();
        try {
            if (item.getStatusTimeline() instanceof List) {
                timeline = (List<StatusTimelineItem>) item.getStatusTimeline();
            }
        } catch (Exception ignored) {}
        timeline.add(new StatusTimelineItem("绣制中", "工坊确认签收，开始绣制", operatorName, remark));
        item.setStatusTimeline(timeline);
        orderItemMapper.updateById(item);
        operationLogService.logOperation(operatorId, "confirm-receive", "logistics", item.getId(), null, null, null, null);
        Order order = orderMapper.selectById(item.getOrderId());
        userMessageService.addMessage(order.getUserId(), "订单进入绣制", "工坊确认签收，进入绣制环节");
    }

    @Override
    public void createProduction(LogisticsProduction record) { 
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        productionMapper.insert(record); 
    }
    @Override
    public void updateProduction(LogisticsProduction record) { 
        record.setUpdatedAt(LocalDateTime.now());
        productionMapper.updateById(record);
        
        // Sync media
        LogisticsProduction existing = productionMapper.selectById(record.getId());
        if (existing != null && existing.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(existing.getOrderItemId());
            if (item != null && record.getMediaUrls() != null) {
                item.setMediaProduction(record.getMediaUrls());
                orderItemMapper.updateById(item);
            }
        }
    }
    @Override
    public void deleteProduction(Long id) { productionMapper.deleteById(id); }

    @Override
    @Transactional
    public void finishProduction(Long id, Long operatorId, String operatorName, String remark) {
        LogisticsProduction pr = productionMapper.selectById(id);
        LogisticsShipment sp = new LogisticsShipment();
        sp.setOrderId(pr.getOrderId());
        sp.setOrderItemId(pr.getOrderItemId());
        sp.setOrderNo(pr.getOrderNo());
        sp.setUserId(pr.getUserId());
        Order orderForSp = orderMapper.selectById(pr.getOrderId());
        String spName = pr.getUserNickname();
        if (orderForSp != null) {
            com.mofadanqing.entity.User uu = userMapper.selectById(orderForSp.getUserId());
            if (uu != null) spName = uu.getUsername();
        }
        sp.setUserNickname(spName);
        sp.setSku(pr.getSku());
        sp.setQuantity(pr.getQuantity());
        sp.setStatus("shipped");
        sp.setLogisticsNo(pr.getLogisticsNo());
        sp.setExpectedFinishTime(pr.getExpectedFinishTime());
        sp.setRemark(remark);
        sp.setCreatedAt(LocalDateTime.now());
        sp.setUpdatedAt(LocalDateTime.now());
        shipmentMapper.insert(sp);
        productionMapper.deleteById(id);
        OrderItem item = orderItemMapper.selectById(pr.getOrderItemId());
        // Sync media from production before it was deleted (from pr object)
        if (pr.getMediaUrls() != null) {
            item.setMediaProduction(pr.getMediaUrls());
        }
        item.setC2mStatus("成品发货");
        item.setCurrentStatus("shipped");
        List<StatusTimelineItem> timeline = new ArrayList<>();
        try {
            if (item.getStatusTimeline() instanceof List) {
                timeline = (List<StatusTimelineItem>) item.getStatusTimeline();
            }
        } catch (Exception ignored) {}
        timeline.add(new StatusTimelineItem("成品发货", "绣制完成，成品发货", operatorName, remark));
        item.setStatusTimeline(timeline);
        orderItemMapper.updateById(item);
        Order order = orderMapper.selectById(item.getOrderId());
        order.setStatus("SHIPPED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        operationLogService.logOperation(operatorId, "finish-production", "logistics", item.getId(), null, null, null, null);
        userMessageService.addMessage(order.getUserId(), "成品已发货", "订单成品已发货，物流单号见详情");
    }

    @Override
    public void createShipment(LogisticsShipment record) { 
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        shipmentMapper.insert(record); 
    }
    @Override
    public void updateShipment(LogisticsShipment record) { 
        record.setUpdatedAt(LocalDateTime.now());
        shipmentMapper.updateById(record);
        
        // Sync media
        LogisticsShipment existing = shipmentMapper.selectById(record.getId());
        if (existing != null && existing.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(existing.getOrderItemId());
            if (item != null && record.getMediaUrls() != null) {
                item.setMediaShipment(record.getMediaUrls());
                orderItemMapper.updateById(item);
            }
        }
    }
    @Override
    public void deleteShipment(Long id) { shipmentMapper.deleteById(id); }
}
