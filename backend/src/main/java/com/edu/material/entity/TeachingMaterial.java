package com.edu.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teaching_material")
public class TeachingMaterial {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    private Long categoryId;
    
    private String fileUrl;
    
    private String fileName;
    
    private String objectName;
    
    private Long fileSize;
    
    private Long uploadUser;
    
    private Integer downloadCount;
    
    private String aiCategory;
    
    private Integer isPrivate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}