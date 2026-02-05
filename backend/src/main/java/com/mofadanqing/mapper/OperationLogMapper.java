package com.mofadanqing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    @Select("SELECT l.*, u.username " +
            "FROM sys_operation_log l " +
            "LEFT JOIN sys_user u ON l.user_id = u.id " +
            "WHERE (#{operation} IS NULL OR l.operation LIKE CONCAT('%', #{operation}, '%')) " +
            "AND (#{username} IS NULL OR u.username LIKE CONCAT('%', #{username}, '%')) " +
            "ORDER BY l.created_at DESC")
    IPage<OperationLog> selectOperationLogPage(Page<OperationLog> page, 
            @Param("operation") String operation, @Param("username") String username);
}