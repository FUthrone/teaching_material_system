package com.edu.material;

import com.edu.material.service.AIClassificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AIClassificationServiceTest {

    @Autowired
    private AIClassificationService aiClassificationService;

    @Test
    public void testClassifyByContent() {
        String fileName = "Python编程基础教程.pdf";
        String content = "Python是一种解释型、面向对象、动态数据类型的高级程序设计语言。" +
                        "Python由Guido van Rossum于1989年底发明，第一个公开发行版发行于1991年。" +
                        "Python语法简洁而清晰，具有丰富和强大的类库。";
        
        Long categoryId = aiClassificationService.classifyByContent(fileName, content);
        
        System.out.println("文件名: " + fileName);
        System.out.println("内容摘要: " + content.substring(0, 50) + "...");
        System.out.println("AI推荐分类ID: " + categoryId);
        
        assert categoryId != null : "分类ID不应为空";
    }

    @Test
    public void testClassifyMathExam() {
        String fileName = "高等数学期末试卷.docx";
        String content = "一、选择题（每题5分，共20分）\n" +
                        "1. 求极限 lim(x→0) sin(x)/x 的值。\n" +
                        "2. 求函数 f(x)=x²+2x+1 的导数。";
        
        Long categoryId = aiClassificationService.classifyByContent(fileName, content);
        
        System.out.println("文件名: " + fileName);
        System.out.println("AI推荐分类ID: " + categoryId);
        
        assert categoryId != null : "分类ID不应为空";
    }

    @Test
    public void testClassifyDatabasePPT() {
        String fileName = "数据库系统原理.pptx";
        String content = "第一章 绪论\n" +
                        "1.1 数据库系统概述\n" +
                        "1.2 数据模型\n" +
                        "1.3 数据库系统结构\n" +
                        "SQL语言基础";
        
        Long categoryId = aiClassificationService.classifyByContent(fileName, content);
        
        System.out.println("文件名: " + fileName);
        System.out.println("AI推荐分类ID: " + categoryId);
        
        assert categoryId != null : "分类ID不应为空";
    }
}
