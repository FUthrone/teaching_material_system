package com.edu.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.material.common.Result;
import com.edu.material.entity.SysPermission;
import com.edu.material.service.SysPermissionService;
import com.edu.material.service.SysRolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysRolePermissionService sysRolePermissionService;

    @GetMapping("/list")
    public Result<Page<SysPermission>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Page<SysPermission> pageParam = new Page<>(page, size);
        return Result.success(sysPermissionService.page(pageParam));
    }

    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysPermission permission) {
        sysPermissionService.save(permission);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        sysPermissionService.updateById(permission);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.removeById(id);
        return Result.success();
    }

    @PostMapping("/role/{roleId}/assign")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        sysRolePermissionService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }
}