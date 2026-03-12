package com.edu.material.controller;

import com.edu.material.common.Result;
import com.edu.material.entity.MaterialCategory;
import com.edu.material.service.MaterialCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private MaterialCategoryService materialCategoryService;

    @GetMapping("/tree")
    public Result<List<MaterialCategory>> getTree() {
        logger.info("获取分类树，当前用户: {}", SecurityContextHolder.getContext().getAuthentication());
        List<MaterialCategory> tree = materialCategoryService.getTree();
        logger.info("分类树数据: {}", tree);
        return Result.success(tree);
    }

    @GetMapping("/{id}")
    public Result<MaterialCategory> getById(@PathVariable Long id) {
        return Result.success(materialCategoryService.getById(id));
    }

    @PostMapping
    public Result<MaterialCategory> create(@RequestBody MaterialCategory category) {
        return Result.success(materialCategoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MaterialCategory category) {
        category.setId(id);
        materialCategoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialCategoryService.removeById(id);
        return Result.success();
    }
}