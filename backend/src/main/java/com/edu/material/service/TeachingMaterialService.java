package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.entity.DownloadRecord;
import com.edu.material.entity.TeachingMaterial;
import com.edu.material.mapper.TeachingMaterialMapper;
import com.edu.material.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@Service
public class TeachingMaterialService extends ServiceImpl<TeachingMaterialMapper, TeachingMaterial> {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private DownloadRecordService downloadRecordService;

    public Page<TeachingMaterial> getMaterialsPage(int page, int size, Long categoryId, String keyword) {
        Page<TeachingMaterial> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TeachingMaterial> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(TeachingMaterial::getIsPrivate, 0);
        
        if (categoryId != null) {
            wrapper.eq(TeachingMaterial::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TeachingMaterial::getTitle, keyword)
                             .or()
                             .like(TeachingMaterial::getDescription, keyword));
        }
        
        wrapper.orderByDesc(TeachingMaterial::getCreateTime);
        return page(pageParam, wrapper);
    }

    public Page<TeachingMaterial> getPersonalFilesPage(int page, int size, Long userId) {
        Page<TeachingMaterial> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TeachingMaterial> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(TeachingMaterial::getUploadUser, userId);
        wrapper.eq(TeachingMaterial::getIsPrivate, 1);
        wrapper.orderByDesc(TeachingMaterial::getCreateTime);
        
        return page(pageParam, wrapper);
    }

    public TeachingMaterial uploadMaterial(TeachingMaterial material, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String objectName = System.currentTimeMillis() + "_" + 
                                java.util.UUID.randomUUID().toString().replace("-", "") + 
                                extension;
            
            String fileUrl = minioUtil.uploadFile(objectName, file);
            
            material.setFileUrl(fileUrl);
            material.setObjectName(objectName);
            material.setFileName(originalFilename);
            material.setFileSize(file.getSize());
            material.setDownloadCount(0);
            
            save(material);
            return material;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public void downloadMaterial(Long materialId, Long userId, String ip, HttpServletResponse response) {
        TeachingMaterial material = getById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }
        try {
            String objectName = material.getObjectName();
            if (objectName == null || objectName.isEmpty()) {
                throw new RuntimeException("文件对象名称不存在");
            }
            InputStream inputStream = minioUtil.downloadFile(objectName);
            
            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(material.getFileName(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
            inputStream.close();
            
            material.setDownloadCount(material.getDownloadCount() + 1);
            updateById(material);
            downloadRecordService.recordDownload(materialId, userId, ip);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    public void deleteMaterial(Long materialId) {
        TeachingMaterial material = getById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }

        try {
            String objectName = material.getObjectName();
            if (objectName != null && !objectName.isEmpty()) {
                minioUtil.deleteFile(objectName);
            }
            removeById(materialId);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    public List<TeachingMaterial> getMaterialsByCategory(Long categoryId) {
        return list(new LambdaQueryWrapper<TeachingMaterial>()
                .eq(TeachingMaterial::getCategoryId, categoryId)
                .orderByDesc(TeachingMaterial::getCreateTime));
    }
}