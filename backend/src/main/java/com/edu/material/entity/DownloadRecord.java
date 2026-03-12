package com.edu.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("download_record")
public class DownloadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long materialId;
    
    private Long userId;
    
    private String downloadIp;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}