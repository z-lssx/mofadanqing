package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.*;
import com.mofadanqing.service.LogisticsService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/logistics")
public class LogisticsAdminController {
    @Autowired private LogisticsService logisticsService;

    private boolean isAdmin(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                User user = JwtUtil.getUserFromToken(token);
                return user != null && "ADMIN".equals(user.getRole());
            }
        } catch (Exception ignored) {}
        return false;
    }

    private User currentUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUserFromToken(token);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @GetMapping("/pack/list")
    public ResponseEntity<Map<String,Object>> listPack(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Long userId,
            @RequestParam(required=false) String nickname,
            @RequestParam(required=false) String orderNo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String startTime,
            @RequestParam(required=false) String endTime,
            HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        IPage<LogisticsPack> p = logisticsService.listPack(new Page<>(page,size), userId, nickname, orderNo, status, startTime, endTime);
        return ok(p);
    }

    @PostMapping("/pack")
    public ResponseEntity<Map<String,Object>> createPack(@RequestBody LogisticsPack record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.createPack(record);
        return ok(null);
    }

    @PutMapping("/pack/{id}")
    public ResponseEntity<Map<String,Object>> updatePack(@PathVariable Long id, @RequestBody LogisticsPack record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        record.setId(id);
        logisticsService.updatePack(record);
        return ok(null);
    }

    @DeleteMapping("/pack/{id}")
    public ResponseEntity<Map<String,Object>> deletePack(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.deletePack(id);
        return ok(null);
    }

    @PostMapping("/pack/{id}/confirm-issue")
    public ResponseEntity<Map<String,Object>> confirmIssue(@PathVariable Long id, @RequestBody Map<String,String> payload, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        User u = currentUser(request);
        String remark = payload.getOrDefault("remark", "");
        logisticsService.confirmIssue(id, u.getId(), u.getUsername(), remark);
        return ok(null);
    }

    @GetMapping(value="/pack/export", produces="text/csv")
    public ResponseEntity<byte[]> exportPack(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).build();
        IPage<LogisticsPack> p = logisticsService.listPack(new Page<>(1,1000), null,null,null,null,null,null);
        String header = "订单号,用户ID,昵称,SKU,数量,状态,备注\n";
        String body = p.getRecords().stream().map(r ->
                String.join(",",
                        safe(r.getOrderNo()),
                        safe(String.valueOf(r.getUserId())),
                        safe(r.getUserNickname()),
                        safe(r.getSku()),
                        safe(String.valueOf(r.getQuantity())),
                        safe(r.getStatus()),
                        safe(r.getRemark())
                )
        ).collect(Collectors.joining("\n"));
        byte[] bytes = (header + body).getBytes(StandardCharsets.UTF_8);
        HttpHeaders hs = new HttpHeaders();
        hs.setContentType(MediaType.valueOf("text/csv"));
        hs.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pack.csv");
        return ResponseEntity.ok().headers(hs).body(bytes);
    }

    @GetMapping("/workshop/list")
    public ResponseEntity<Map<String,Object>> listWorkshop(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Long userId,
            @RequestParam(required=false) String nickname,
            @RequestParam(required=false) String orderNo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String startTime,
            @RequestParam(required=false) String endTime,
            HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        IPage<LogisticsWorkshop> p = logisticsService.listWorkshop(new Page<>(page,size), userId, nickname, orderNo, status, startTime, endTime);
        return ok(p);
    }

    @PostMapping("/workshop")
    public ResponseEntity<Map<String,Object>> createWorkshop(@RequestBody LogisticsWorkshop record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.createWorkshop(record);
        return ok(null);
    }

    @PutMapping("/workshop/{id}")
    public ResponseEntity<Map<String,Object>> updateWorkshop(@PathVariable Long id, @RequestBody LogisticsWorkshop record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        record.setId(id);
        logisticsService.updateWorkshop(record);
        return ok(null);
    }

    @DeleteMapping("/workshop/{id}")
    public ResponseEntity<Map<String,Object>> deleteWorkshop(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.deleteWorkshop(id);
        return ok(null);
    }

    @PostMapping("/workshop/{id}/confirm-receive")
    public ResponseEntity<Map<String,Object>> confirmReceive(@PathVariable Long id, @RequestBody Map<String,String> payload, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        User u = currentUser(request);
        String remark = payload.getOrDefault("remark", "");
        logisticsService.confirmReceive(id, u.getId(), u.getUsername(), remark);
        return ok(null);
    }

    @GetMapping("/production/list")
    public ResponseEntity<Map<String,Object>> listProduction(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Long userId,
            @RequestParam(required=false) String nickname,
            @RequestParam(required=false) String orderNo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String startTime,
            @RequestParam(required=false) String endTime,
            HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        IPage<LogisticsProduction> p = logisticsService.listProduction(new Page<>(page,size), userId, nickname, orderNo, status, startTime, endTime);
        return ok(p);
    }

    @PostMapping("/production")
    public ResponseEntity<Map<String,Object>> createProduction(@RequestBody LogisticsProduction record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.createProduction(record);
        return ok(null);
    }

    @PutMapping("/production/{id}")
    public ResponseEntity<Map<String,Object>> updateProduction(@PathVariable Long id, @RequestBody LogisticsProduction record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        record.setId(id);
        logisticsService.updateProduction(record);
        return ok(null);
    }

    @DeleteMapping("/production/{id}")
    public ResponseEntity<Map<String,Object>> deleteProduction(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.deleteProduction(id);
        return ok(null);
    }

    @PostMapping("/production/{id}/finish-production")
    public ResponseEntity<Map<String,Object>> finishProduction(@PathVariable Long id, @RequestBody Map<String,String> payload, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        User u = currentUser(request);
        String remark = payload.getOrDefault("remark", "");
        logisticsService.finishProduction(id, u.getId(), u.getUsername(), remark);
        return ok(null);
    }

    @GetMapping("/shipment/list")
    public ResponseEntity<Map<String,Object>> listShipment(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Long userId,
            @RequestParam(required=false) String nickname,
            @RequestParam(required=false) String orderNo,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String startTime,
            @RequestParam(required=false) String endTime,
            HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        IPage<LogisticsShipment> p = logisticsService.listShipment(new Page<>(page,size), userId, nickname, orderNo, status, startTime, endTime);
        return ok(p);
    }

    @PostMapping("/shipment")
    public ResponseEntity<Map<String,Object>> createShipment(@RequestBody LogisticsShipment record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.createShipment(record);
        return ok(null);
    }

    @PutMapping("/shipment/{id}")
    public ResponseEntity<Map<String,Object>> updateShipment(@PathVariable Long id, @RequestBody LogisticsShipment record, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        record.setId(id);
        logisticsService.updateShipment(record);
        return ok(null);
    }

    @DeleteMapping("/shipment/{id}")
    public ResponseEntity<Map<String,Object>> deleteShipment(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return error(403,"无权限");
        logisticsService.deleteShipment(id);
        return ok(null);
    }

    private String safe(String s) { return s == null ? "" : s.replaceAll(",", " "); }
    private ResponseEntity<Map<String,Object>> ok(Object data) {
        Map<String,Object> res = new HashMap<>();
        res.put("code",200);
        res.put("message","success");
        res.put("data", data);
        return ResponseEntity.ok(res);
    }
    private ResponseEntity<Map<String,Object>> error(int code, String msg) {
        Map<String,Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", msg);
        return ResponseEntity.status(code).body(res);
    }
}
