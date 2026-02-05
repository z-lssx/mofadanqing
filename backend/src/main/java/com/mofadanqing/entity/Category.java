package com.mofadanqing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pms_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private Integer sortOrder;
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}