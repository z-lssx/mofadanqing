package com.mofadanqing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.LogisticsPack;
import com.mofadanqing.entity.LogisticsWorkshop;
import com.mofadanqing.entity.LogisticsProduction;
import com.mofadanqing.entity.LogisticsShipment;

public interface LogisticsService {
    IPage<LogisticsPack> listPack(Page<LogisticsPack> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime);
    IPage<LogisticsWorkshop> listWorkshop(Page<LogisticsWorkshop> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime);
    IPage<LogisticsProduction> listProduction(Page<LogisticsProduction> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime);
    IPage<LogisticsShipment> listShipment(Page<LogisticsShipment> page, Long userId, String nickname, String orderNo, String status, String startTime, String endTime);

    void createPack(LogisticsPack record);
    void updatePack(LogisticsPack record);
    void deletePack(Long id);
    void confirmIssue(Long id, Long operatorId, String operatorName, String remark);

    void createWorkshop(LogisticsWorkshop record);
    void updateWorkshop(LogisticsWorkshop record);
    void deleteWorkshop(Long id);
    void confirmReceive(Long id, Long operatorId, String operatorName, String remark);

    void createProduction(LogisticsProduction record);
    void updateProduction(LogisticsProduction record);
    void deleteProduction(Long id);
    void finishProduction(Long id, Long operatorId, String operatorName, String remark);

    void createShipment(LogisticsShipment record);
    void updateShipment(LogisticsShipment record);
    void deleteShipment(Long id);
}
