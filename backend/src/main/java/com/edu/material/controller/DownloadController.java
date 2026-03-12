package com.edu.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.material.common.Result;
import com.edu.material.entity.DownloadRecord;
import com.edu.material.service.DownloadRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

    @Autowired
    private DownloadRecordService downloadRecordService;

    @GetMapping("/records")
    public Result<Page<DownloadRecord>> getRecords(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) Long materialId) {
        return Result.success(downloadRecordService.getDownloadRecords(page, size, userId, materialId));
    }

    @GetMapping("/count/material/{materialId}")
    public Result<Long> getCountByMaterial(@PathVariable Long materialId) {
        return Result.success(downloadRecordService.getDownloadCountByMaterial(materialId));
    }

    @GetMapping("/count/user/{userId}")
    public Result<Long> getCountByUser(@PathVariable Long userId) {
        return Result.success(downloadRecordService.getDownloadCountByUser(userId));
    }
}