package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.entity.DownloadRecord;
import com.edu.material.mapper.DownloadRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DownloadRecordService extends ServiceImpl<DownloadRecordMapper, DownloadRecord> {

    public void recordDownload(Long materialId, Long userId, String ip) {
        DownloadRecord record = new DownloadRecord();
        record.setMaterialId(materialId);
        record.setUserId(userId);
        record.setDownloadIp(ip);
        save(record);
    }

    public Page<DownloadRecord> getDownloadRecords(int page, int size, Long userId, Long materialId) {
        Page<DownloadRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DownloadRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(DownloadRecord::getUserId, userId);
        }
        
        if (materialId != null) {
            wrapper.eq(DownloadRecord::getMaterialId, materialId);
        }
        
        wrapper.orderByDesc(DownloadRecord::getCreateTime);
        return page(pageParam, wrapper);
    }

    public Long getDownloadCountByMaterial(Long materialId) {
        return count(new LambdaQueryWrapper<DownloadRecord>()
                .eq(DownloadRecord::getMaterialId, materialId));
    }

    public Long getDownloadCountByUser(Long userId) {
        return count(new LambdaQueryWrapper<DownloadRecord>()
                .eq(DownloadRecord::getUserId, userId));
    }
}