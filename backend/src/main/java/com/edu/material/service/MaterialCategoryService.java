package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.entity.MaterialCategory;
import com.edu.material.mapper.MaterialCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialCategoryService extends ServiceImpl<MaterialCategoryMapper, MaterialCategory> {

    public List<MaterialCategory> getTree() {
        List<MaterialCategory> allCategories = list();
        return buildTree(allCategories, 0L);
    }

    private List<MaterialCategory> buildTree(List<MaterialCategory> categories, Long parentId) {
        return categories.stream()
                .filter(category -> category.getParentId().equals(parentId))
                .peek(category -> category.setChildren(buildTree(categories, category.getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    public MaterialCategory createCategory(MaterialCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getLevel() == null) {
            category.setLevel(1);
        }
        save(category);
        return category;
    }
}