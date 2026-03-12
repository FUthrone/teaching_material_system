package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.entity.DownloadRecord;
import com.edu.material.mapper.DownloadRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class DownloadRecordService extends ServiceImpl<DownloadRecordMapper, DownloadRecord> {


    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(Long materialId, Long userId, String ip) {
        validateDownloadRecord(materialId, userId, ip);
        
        DownloadRecord record = buildDownloadRecord(materialId, userId, ip);
        save(record);
        
        logDownloadRecord(record);
    }

    private void validateDownloadRecord(Long materialId, Long userId, String ip) {
        if (materialId == null || materialId <= 0) {
            throw new IllegalArgumentException("资料ID不能为空或小于等于0");
        }
    }

    private DownloadRecord buildDownloadRecord(Long materialId, Long userId, String ip) {
        DownloadRecord record = new DownloadRecord();
        record.setMaterialId(materialId);
        record.setUserId(userId);
        record.setDownloadIp(ip);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private void logDownloadRecord(DownloadRecord record) {
        System.out.println("=== 下载记录保存成功 ===");
        System.out.println("资料ID: " + record.getMaterialId());
        System.out.println("用户ID: " + record.getUserId());
        System.out.println("下载IP: " + record.getDownloadIp());
        System.out.println("下载时间: " + record.getCreateTime());
        System.out.println("记录ID: " + record.getId());
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