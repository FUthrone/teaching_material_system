package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.entity.SharedMaterial;
import com.edu.material.entity.TeachingMaterial;
import com.edu.material.mapper.SharedMaterialMapper;
import com.edu.material.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SharedMaterialService extends ServiceImpl<SharedMaterialMapper, SharedMaterial> {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private TeachingMaterialService teachingMaterialService;

    public SharedMaterial createShare(Long materialId, Long creatorId, String password, Integer expireDays, Integer maxDownloads) {
        TeachingMaterial material = teachingMaterialService.getById(materialId);
        if (material == null) {
            throw new RuntimeException("资料不存在");
        }

        SharedMaterial share = new SharedMaterial();
        share.setMaterialId(materialId);
        share.setShareCode(generateShareCode());
        share.setSharePassword(password);
        share.setCreatorId(creatorId);
        
        if (expireDays != null && expireDays > 0) {
            share.setExpireTime(LocalDateTime.now().plusDays(expireDays));
        }
        
        share.setMaxDownloads(maxDownloads != null ? maxDownloads : -1);
        share.setCurrentDownloads(0);
        share.setStatus(1);
        
        save(share);
        return share;
    }

    public SharedMaterial getByShareCode(String shareCode) {
        return getOne(new LambdaQueryWrapper<SharedMaterial>()
                .eq(SharedMaterial::getShareCode, shareCode)
                .eq(SharedMaterial::getStatus, 1));
    }

    public boolean validatePassword(SharedMaterial share, String password) {
        if (share.getSharePassword() == null || share.getSharePassword().isEmpty()) {
            return true;
        }
        return share.getSharePassword().equals(password);
    }

    public boolean isShareValid(SharedMaterial share) {
        if (share.getStatus() != 1) {
            return false;
        }

        if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
            share.setStatus(0);
            updateById(share);
            return false;
        }

        if (share.getMaxDownloads() > 0 && share.getCurrentDownloads() >= share.getMaxDownloads()) {
            share.setStatus(0);
            updateById(share);
            return false;
        }

        return true;
    }

    public void downloadSharedMaterial(String shareCode, String password, HttpServletResponse response) {
        SharedMaterial share = getByShareCode(shareCode);
        
        if (share == null) {
            throw new RuntimeException("分享链接不存在或已失效");
        }

        if (!isShareValid(share)) {
            throw new RuntimeException("分享链接已过期或已达到下载上限");
        }

        if (!validatePassword(share, password)) {
            throw new RuntimeException("访问密码错误");
        }

        TeachingMaterial material = teachingMaterialService.getById(share.getMaterialId());
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

            share.setCurrentDownloads(share.getCurrentDownloads() + 1);
            updateById(share);

            material.setDownloadCount(material.getDownloadCount() + 1);
            teachingMaterialService.updateById(material);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
