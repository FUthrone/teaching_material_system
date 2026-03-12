package com.edu.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.material.common.Result;
import com.edu.material.entity.TeachingMaterial;
import com.edu.material.service.TeachingMaterialService;
import com.edu.material.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private TeachingMaterialService teachingMaterialService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<Page<TeachingMaterial>> list(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword) {
        return Result.success(teachingMaterialService.getMaterialsPage(page, size, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<TeachingMaterial> getById(@PathVariable Long id) {
        return Result.success(teachingMaterialService.getById(id));
    }

    @PostMapping("/upload")
    public Result<TeachingMaterial> upload(@RequestParam("title") String title,
                                           @RequestParam("description") String description,
                                           @RequestParam("categoryId") Long categoryId,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(jwt);
            
            TeachingMaterial material = new TeachingMaterial();
            material.setTitle(title);
            material.setDescription(description);
            material.setCategoryId(categoryId);
            material.setUploadUser(userId);
            
            TeachingMaterial result = teachingMaterialService.uploadMaterial(material, file);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        Long userId = null;
        
        System.out.println("=== 下载资料 ===");
        System.out.println("资料ID: " + id);
        System.out.println("Authorization Header: " + token);
        
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                System.out.println("JWT Token: " + jwt);
                userId = jwtUtil.getUserIdFromToken(jwt);
                System.out.println("从JWT提取的用户ID: " + userId);
            } catch (Exception e) {
                System.out.println("JWT解析失败: " + e.getMessage());
            }
        }
        
        String ip = getClientIp(request);
        System.out.println("下载IP: " + ip);
        System.out.println("传递给downloadMaterial的userId: " + userId);
        
        teachingMaterialService.downloadMaterial(id, userId, ip, response);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teachingMaterialService.deleteMaterial(id);
        return Result.success();
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<TeachingMaterial>> getByCategory(@PathVariable Long categoryId) {
        return Result.success(teachingMaterialService.getMaterialsByCategory(categoryId));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}