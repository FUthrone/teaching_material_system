package com.edu.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shared_material")
public class SharedMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long materialId;
    
    private String shareCode;
    
    private String sharePassword;
    
    private Long creatorId;
    
    private LocalDateTime expireTime;
    
    private Integer maxDownloads;
    
    private Integer currentDownloads;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
