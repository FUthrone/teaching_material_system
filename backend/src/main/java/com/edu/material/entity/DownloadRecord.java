package com.edu.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("download_record")
public class DownloadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("material_id")
    private Long materialId;
    
    @TableField("user_id")
    private Long userId;
    
    private String downloadIp;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}