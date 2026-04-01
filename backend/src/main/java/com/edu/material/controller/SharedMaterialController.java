package com.edu.material.controller;

import com.edu.material.common.Result;
import com.edu.material.entity.SharedMaterial;
import com.edu.material.entity.TeachingMaterial;
import com.edu.material.service.SharedMaterialService;
import com.edu.material.service.TeachingMaterialService;
import com.edu.material.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
public class SharedMaterialController {

    @Autowired
    private SharedMaterialService sharedMaterialService;

    @Autowired
    private TeachingMaterialService teachingMaterialService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Map<String, Object>> createShare(
            @RequestParam Long materialId,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Integer expireDays,
            @RequestParam(required = false) Integer maxDownloads,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(jwt);

            SharedMaterial share = sharedMaterialService.createShare(
                    materialId, userId, password, expireDays, maxDownloads);

            Map<String, Object> result = new HashMap<>();
            result.put("shareCode", share.getShareCode());
            result.put("shareId", share.getId());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{shareCode}")
    public Result<Map<String, Object>> getShareInfo(
            @PathVariable String shareCode,
            @RequestParam(required = false) String password) {
        try {
            SharedMaterial share = sharedMaterialService.getByShareCode(shareCode);
            
            if (share == null) {
                return Result.error("分享链接不存在或已失效");
            }

            if (!sharedMaterialService.isShareValid(share)) {
                return Result.error("分享链接已过期或已达到下载上限");
            }

            TeachingMaterial material = teachingMaterialService.getById(share.getMaterialId());
            if (material == null) {
                return Result.error("资料不存在");
            }

            boolean needPassword = share.getSharePassword() != null && !share.getSharePassword().isEmpty();
            
            if (needPassword && password == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("needPassword", true);
                return Result.success(result);
            }

            if (!sharedMaterialService.validatePassword(share, password)) {
                return Result.error("访问密码错误");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("title", material.getTitle());
            result.put("description", material.getDescription());
            result.put("fileName", material.getFileName());
            result.put("fileSize", material.getFileSize());
            result.put("downloadCount", material.getDownloadCount());
            result.put("needPassword", false);
            result.put("expireTime", share.getExpireTime());
            result.put("maxDownloads", share.getMaxDownloads());
            result.put("currentDownloads", share.getCurrentDownloads());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/download/{shareCode}")
    public void downloadShare(
            @PathVariable String shareCode,
            @RequestParam(required = false) String password,
            HttpServletResponse response) {
        try {
            sharedMaterialService.downloadSharedMaterial(shareCode, password, response);
        } catch (Exception e) {
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
