package com.edu.material.service;

import com.edu.material.entity.MaterialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIClassificationService {

    @Value("${ai.enabled:false}")
    private Boolean aiEnabled;

    @Value("${ai.aliyun.api-key:}")
    private String aliyunApiKey;

    @Value("${ai.aliyun.model:qwen-turbo}")
    private String aliyunModel;

    @Autowired
    private MaterialCategoryService categoryService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Long classifyByContent(String fileName, String content) {
        if (!aiEnabled) {
            return classifyByRules(fileName, content);
        }

        try {
            String categoryName = callAIService(fileName, content);
            if (categoryName != null && !categoryName.isEmpty()) {
                return findOrCreateCategory(categoryName);
            }
        } catch (Exception e) {
            System.err.println("AI分类失败，使用规则引擎: " + e.getMessage());
        }

        return classifyByRules(fileName, content);
    }

    private String callAIService(String fileName, String content) throws Exception {
        String prompt = buildPrompt(fileName, content);
        return callAliyunAI(prompt);
    }

    private String callAliyunAI(String prompt) throws Exception {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", new Object[]{message});
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aliyunModel);
        requestBody.put("input", input);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + aliyunApiKey);
        headers.set("Content-Type", "application/json");
        
        org.springframework.http.HttpEntity<Map<String, Object>> entity = 
            new org.springframework.http.HttpEntity<>(requestBody, headers);
        
        String response = restTemplate.postForObject(url, entity, String.class);
        JsonNode jsonNode = objectMapper.readTree(response);
        
        if (jsonNode.has("output") && jsonNode.get("output").has("text")) {
            return jsonNode.get("output").get("text").asText().trim();
        }
        
        if (jsonNode.has("output") && jsonNode.get("output").has("choices")) {
            JsonNode choices = jsonNode.get("output").get("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode choice = choices.get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    return choice.get("message").get("content").asText().trim();
                }
            }
        }
        
        return null;
    }

    private String buildPrompt(String fileName, String content) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个高校教学资源分类专家，精通计算机科学、文学、影视等领域。请分析以下资料，并给出最合适的分类。\n\n");
        
        prompt.append("分类指导：\n");
        prompt.append("1. 文学作品分类：\n");
        prompt.append("   - 中国现当代文学作品（如《我与地坛》《活着》《平凡的世界》等）归类为\"现当代文学\"\n");
        prompt.append("   - 中国古代文学作品（如《红楼梦》《西游记》等）归类为\"古代文学\"\n");
        prompt.append("   - 外国文学作品根据国家归类（如莎士比亚作品归为\"英美文学\"，村上春树作品归为\"日本文学\"）\n");
        prompt.append("   - 诗歌、散文、小说等按体裁归类到相应分类\n\n");
        
        prompt.append("2. 计算机科学分类：\n");
        prompt.append("   - 编程语言教程归为具体语言分类（如 Python 教程归为\"Python\"）\n");
        prompt.append("   - AI 相关内容归为\"人工智能\"下的具体分类\n");
        prompt.append("   - 数据结构、算法、数据库等基础知识归为相应分类\n\n");
        
        prompt.append("3. 影视分类：\n");
        prompt.append("   - 电影相关资料归为\"电影\"下的具体分类\n");
        prompt.append("   - 动画相关资料归为\"动画\"下的具体分类\n");
        prompt.append("   - 影视制作技术归为相应的技术分类\n\n");
        
        prompt.append("4. 资料类型：\n");
        prompt.append("   - 课件、教案、试题等教学资料归为\"资料类型\"下的相应分类\n\n");
        
        prompt.append("文件名：").append(fileName).append("\n\n");
        
        if (content != null && !content.isEmpty()) {
            int maxLength = 3000;
            String truncatedContent = content.length() > maxLength ? 
                content.substring(0, maxLength) + "..." : content;
            prompt.append("文件内容摘要：\n").append(truncatedContent).append("\n\n");
        }
        
        prompt.append("已有分类体系：\n");
        List<MaterialCategory> categories = categoryService.getTree();
        appendCategoryTree(categories, prompt, 0);
        prompt.append("\n\n");
        
        prompt.append("请根据文件名和内容，判断最合适的分类。\n");
        prompt.append("直接返回分类名称（如：现当代文学、Python、电影理论），不要解释。\n");
        prompt.append("如果需要创建新分类，返回格式：新分类名|父分类名");
        
        return prompt.toString();
    }

    private void appendCategoryTree(List<MaterialCategory> categories, StringBuilder prompt, int level) {
        String indent = "";
        for (int i = 0; i < level; i++) {
            indent += "  ";
        }
        for (MaterialCategory category : categories) {
            prompt.append(indent).append("- ").append(category.getCategoryName()).append("\n");
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                appendCategoryTree(category.getChildren(), prompt, level + 1);
            }
        }
    }

    private void appendCategoryNames(List<MaterialCategory> categories, StringBuilder prompt) {
        for (MaterialCategory category : categories) {
            prompt.append(category.getCategoryName()).append("、");
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                appendCategoryNames(category.getChildren(), prompt);
            }
        }
    }

    private Long classifyByRules(String fileName, String content) {
        String lowerFileName = fileName.toLowerCase();
        String combinedText = lowerFileName;
        
        if (content != null && !content.isEmpty()) {
            combinedText += " " + content.toLowerCase();
        }
        
        if (lowerFileName.contains("试卷") || lowerFileName.contains("考试") || lowerFileName.contains("习题") || lowerFileName.contains("试题")) {
            return findOrCreateCategory("试题库");
        }
        if (lowerFileName.contains("课件") || lowerFileName.contains("ppt")) {
            return findOrCreateCategory("课件");
        }
        if (lowerFileName.contains("教案") || lowerFileName.contains("教学设计")) {
            return findOrCreateCategory("教案");
        }
        if (lowerFileName.contains("实验") || lowerFileName.contains("实践") || lowerFileName.contains("实训")) {
            return findOrCreateCategory("实验指导");
        }
        if (lowerFileName.contains("论文") || lowerFileName.contains("研究") || lowerFileName.contains("毕业设计")) {
            return findOrCreateCategory("学术论文");
        }
        if (lowerFileName.contains("视频") || lowerFileName.contains("mp4") || lowerFileName.contains("avi") || lowerFileName.contains("教学录像")) {
            return findOrCreateCategory("教学视频");
        }
        if (lowerFileName.contains("教材") || lowerFileName.contains("课本")) {
            return findOrCreateCategory("教材");
        }
        
        if (combinedText.contains("数据结构") || combinedText.contains("链表") || combinedText.contains("二叉树") || combinedText.contains("哈希表")) {
            return findOrCreateCategory("数据结构");
        }
        if (combinedText.contains("算法") && (combinedText.contains("设计") || combinedText.contains("分析") || combinedText.contains("排序") || combinedText.contains("查找"))) {
            return findOrCreateCategory("算法");
        }
        if (combinedText.contains("操作系统") || combinedText.contains("linux") || combinedText.contains("进程管理") || combinedText.contains("内存管理")) {
            return findOrCreateCategory("操作系统");
        }
        if (combinedText.contains("计算机网络") || combinedText.contains("tcp/ip") || combinedText.contains("http") || combinedText.contains("网络协议")) {
            return findOrCreateCategory("计算机网络");
        }
        if (combinedText.contains("数据库") || combinedText.contains("sql") || combinedText.contains("mysql") || combinedText.contains("oracle") || combinedText.contains("mongodb")) {
            return findOrCreateCategory("数据库");
        }
        if (combinedText.contains("编译原理") || combinedText.contains("编译器") || combinedText.contains("词法分析") || combinedText.contains("语法分析")) {
            return findOrCreateCategory("编译原理");
        }
        if (combinedText.contains("计算机组成") || combinedText.contains("cpu") || combinedText.contains("存储器") || combinedText.contains("指令系统")) {
            return findOrCreateCategory("计算机组成原理");
        }
        
        if (combinedText.contains("机器学习") || combinedText.contains("深度学习") || combinedText.contains("神经网络") || combinedText.contains("模型训练")) {
            return findOrCreateCategory("机器学习");
        }
        if (combinedText.contains("计算机视觉") || combinedText.contains("图像识别") || combinedText.contains("目标检测") || combinedText.contains("opencv")) {
            return findOrCreateCategory("计算机视觉");
        }
        if (combinedText.contains("自然语言处理") || combinedText.contains("nlp") || combinedText.contains("文本分析") || combinedText.contains("情感分析")) {
            return findOrCreateCategory("自然语言处理");
        }
        if (combinedText.contains("大数据") || combinedText.contains("hadoop") || combinedText.contains("spark") || combinedText.contains("数据挖掘")) {
            return findOrCreateCategory("大数据");
        }
        
        if (combinedText.contains("python")) {
            return findOrCreateCategory("Python");
        }
        if (combinedText.contains("java") && !combinedText.contains("javascript")) {
            return findOrCreateCategory("Java");
        }
        if (combinedText.contains("c语言") || combinedText.contains("c程序")) {
            return findOrCreateCategory("C语言");
        }
        if (combinedText.contains("c++") || combinedText.contains("cpp")) {
            return findOrCreateCategory("C++");
        }
        if (combinedText.contains("javascript") || combinedText.contains("js") || combinedText.contains("typescript")) {
            return findOrCreateCategory("JavaScript");
        }
        if (combinedText.contains("golang") || combinedText.contains("go语言")) {
            return findOrCreateCategory("Go");
        }
        if (combinedText.contains("rust")) {
            return findOrCreateCategory("Rust");
        }
        
        if (combinedText.contains("前端") || combinedText.contains("vue") || combinedText.contains("react") || combinedText.contains("angular")) {
            return findOrCreateCategory("前端开发");
        }
        if (combinedText.contains("后端") || combinedText.contains("spring") || combinedText.contains("django") || combinedText.contains("express")) {
            return findOrCreateCategory("后端开发");
        }
        if (combinedText.contains("移动开发") || combinedText.contains("android") || combinedText.contains("ios") || combinedText.contains("flutter")) {
            return findOrCreateCategory("移动开发");
        }
        if (combinedText.contains("游戏开发") || combinedText.contains("unity") || combinedText.contains("unreal") || combinedText.contains("游戏引擎")) {
            return findOrCreateCategory("游戏开发");
        }
        if (combinedText.contains("软件工程") || combinedText.contains("软件测试") || combinedText.contains("敏捷开发")) {
            return findOrCreateCategory("软件工程");
        }
        
        if (combinedText.contains("我与地坛") || combinedText.contains("活着") || combinedText.contains("平凡的世界") || 
            combinedText.contains("围城") || combinedText.contains("白鹿原") || combinedText.contains("红高粱") ||
            combinedText.contains("许三观卖血记") || combinedText.contains("兄弟") || combinedText.contains("在细雨中呼喊")) {
            return findOrCreateCategory("现当代文学");
        }
        if (combinedText.contains("红楼梦") || combinedText.contains("西游记") || combinedText.contains("水浒传") || 
            combinedText.contains("三国演义") || combinedText.contains("儒林外史") || combinedText.contains("聊斋志异")) {
            return findOrCreateCategory("古代文学");
        }
        if (combinedText.contains("诗歌") || combinedText.contains("诗词") || combinedText.contains("唐诗") || 
            combinedText.contains("宋词") || combinedText.contains("现代诗")) {
            return findOrCreateCategory("诗歌");
        }
        if (combinedText.contains("散文") || combinedText.contains("随笔") || combinedText.contains("杂文")) {
            return findOrCreateCategory("散文");
        }
        if (combinedText.contains("小说") && (combinedText.contains("创作") || combinedText.contains("写作") || combinedText.contains("鉴赏"))) {
            return findOrCreateCategory("小说");
        }
        if (combinedText.contains("戏剧") || combinedText.contains("话剧") || combinedText.contains("剧本")) {
            return findOrCreateCategory("戏剧文学");
        }
        
        if (combinedText.contains("莎士比亚") || combinedText.contains("哈姆雷特") || combinedText.contains("罗密欧与朱丽叶")) {
            return findOrCreateCategory("英美文学");
        }
        if (combinedText.contains("村上春树") || combinedText.contains("挪威的森林") || combinedText.contains("川端康成")) {
            return findOrCreateCategory("日本文学");
        }
        if (combinedText.contains("托尔斯泰") || combinedText.contains("陀思妥耶夫斯基") || combinedText.contains("战争与和平")) {
            return findOrCreateCategory("俄罗斯文学");
        }
        if (combinedText.contains("百年孤独") || combinedText.contains("马尔克斯")) {
            return findOrCreateCategory("拉美文学");
        }
        
        if (combinedText.contains("文学理论") || combinedText.contains("文艺学") || combinedText.contains("文学批评") || combinedText.contains("文学研究")) {
            return findOrCreateCategory("文学理论");
        }
        if (combinedText.contains("语言学") || combinedText.contains("语法") || combinedText.contains("音韵") || combinedText.contains("词汇学")) {
            return findOrCreateCategory("语言学");
        }
        
        if (combinedText.contains("电影史") || combinedText.contains("电影发展") || combinedText.contains("电影运动")) {
            return findOrCreateCategory("电影史");
        }
        if (combinedText.contains("电影理论") || combinedText.contains("电影美学") || combinedText.contains("电影批评")) {
            return findOrCreateCategory("电影理论");
        }
        if (combinedText.contains("导演") || combinedText.contains("导演艺术") || combinedText.contains("导演创作")) {
            return findOrCreateCategory("导演艺术");
        }
        if (combinedText.contains("编剧") || combinedText.contains("剧本创作") || combinedText.contains("剧本写作")) {
            return findOrCreateCategory("编剧");
        }
        if (combinedText.contains("摄影") || combinedText.contains("摄像") || combinedText.contains("镜头语言")) {
            return findOrCreateCategory("摄影");
        }
        if (combinedText.contains("剪辑") || combinedText.contains("视频剪辑") || combinedText.contains("后期制作")) {
            return findOrCreateCategory("剪辑");
        }
        if (combinedText.contains("特效") || combinedText.contains("视觉特效") || combinedText.contains("vfx") || combinedText.contains("cgi")) {
            return findOrCreateCategory("特效");
        }
        
        if (combinedText.contains("动画制作") || combinedText.contains("动画设计")) {
            return findOrCreateCategory("动画制作");
        }
        if (combinedText.contains("二维动画") || combinedText.contains("flash动画") || combinedText.contains("手绘动画")) {
            return findOrCreateCategory("二维动画");
        }
        if (combinedText.contains("三维动画") || combinedText.contains("3d动画") || combinedText.contains("maya") || combinedText.contains("blender")) {
            return findOrCreateCategory("三维动画");
        }
        if (combinedText.contains("动画理论") || combinedText.contains("动画史")) {
            return findOrCreateCategory("动画理论");
        }
        
        if (combinedText.contains("纪录片")) {
            return findOrCreateCategory("纪录片");
        }
        if (combinedText.contains("电视剧") || combinedText.contains("电视剧制作")) {
            return findOrCreateCategory("电视剧");
        }
        
        return findOrCreateCategory("其他");
    }

    private Long findOrCreateCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = "其他";
        }
        
        categoryName = categoryName.trim();
        
        if (categoryName.contains("|")) {
            String[] parts = categoryName.split("\\|");
            if (parts.length == 2) {
                String newCategoryName = parts[0].trim();
                String parentCategoryName = parts[1].trim();
                
                List<MaterialCategory> categories = categoryService.getTree();
                MaterialCategory parentCategory = findCategoryByName(categories, parentCategoryName);
                
                if (parentCategory != null) {
                    Long existingId = searchCategoryExact(categories, newCategoryName);
                    if (existingId != null) {
                        return existingId;
                    }
                    
                    MaterialCategory newCategory = new MaterialCategory();
                    newCategory.setCategoryName(newCategoryName);
                    newCategory.setParentId(parentCategory.getId());
                    newCategory.setLevel(parentCategory.getLevel() + 1);
                    newCategory.setPath(parentCategory.getPath() + "/" + newCategoryName);
                    newCategory.setSort(0);
                    
                    categoryService.createCategory(newCategory);
                    
                    return newCategory.getId();
                }
            }
        }
        
        List<MaterialCategory> categories = categoryService.getTree();
        
        Long exactMatchId = searchCategoryExact(categories, categoryName);
        if (exactMatchId != null) {
            return exactMatchId;
        }
        
        Long fuzzyMatchId = searchCategoryFuzzy(categories, categoryName);
        if (fuzzyMatchId != null) {
            return fuzzyMatchId;
        }
        
        return createCategoryIntelligently(categories, categoryName);
    }

    private Long searchCategoryExact(List<MaterialCategory> categories, String categoryName) {
        for (MaterialCategory category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                return category.getId();
            }
            
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                Long foundId = searchCategoryExact(category.getChildren(), categoryName);
                if (foundId != null) {
                    return foundId;
                }
            }
        }
        return null;
    }

    private Long searchCategoryFuzzy(List<MaterialCategory> categories, String categoryName) {
        double maxSimilarity = 0.0;
        Long bestMatchId = null;
        
        for (MaterialCategory category : categories) {
            double similarity = calculateSimilarity(category.getCategoryName(), categoryName);
            if (similarity > maxSimilarity && similarity >= 0.6) {
                maxSimilarity = similarity;
                bestMatchId = category.getId();
            }
            
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                Long childMatchId = searchCategoryFuzzy(category.getChildren(), categoryName);
                if (childMatchId != null) {
                    return childMatchId;
                }
            }
        }
        
        return bestMatchId;
    }

    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        String s1 = str1.toLowerCase().trim();
        String s2 = str2.toLowerCase().trim();
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        if (s1.contains(s2) || s2.contains(s1)) {
            return 0.8;
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    ) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    private Long createCategoryIntelligently(List<MaterialCategory> categories, String categoryName) {
        Long parentId = 0L;
        int level = 1;
        String path = "/" + categoryName;
        
        MaterialCategory bestParent = findBestParentCategory(categories, categoryName);
        if (bestParent != null) {
            parentId = bestParent.getId();
            level = bestParent.getLevel() + 1;
            path = bestParent.getPath() + "/" + categoryName;
        }
        
        MaterialCategory newCategory = new MaterialCategory();
        newCategory.setCategoryName(categoryName);
        newCategory.setParentId(parentId);
        newCategory.setLevel(level);
        newCategory.setPath(path);
        newCategory.setSort(0);
        
        categoryService.createCategory(newCategory);
        
        return newCategory.getId();
    }

    private MaterialCategory findBestParentCategory(List<MaterialCategory> categories, String categoryName) {
        String lowerName = categoryName.toLowerCase();
        
        if (lowerName.contains("数据结构") || lowerName.contains("算法") || 
            lowerName.contains("数据库") || lowerName.contains("操作系统") ||
            lowerName.contains("计算机网络") || lowerName.contains("编译原理") ||
            lowerName.contains("计算机组成") || lowerName.contains("软件工程") ||
            lowerName.contains("机器学习") || lowerName.contains("深度学习") ||
            lowerName.contains("计算机视觉") || lowerName.contains("自然语言处理") ||
            lowerName.contains("大数据") || lowerName.contains("python") ||
            lowerName.contains("java") || lowerName.contains("编程") ||
            lowerName.contains("程序") || lowerName.contains("前端") ||
            lowerName.contains("后端") || lowerName.contains("移动开发") ||
            lowerName.contains("游戏开发") || lowerName.contains("c语言") ||
            lowerName.contains("c++") || lowerName.contains("javascript") ||
            lowerName.contains("golang") || lowerName.contains("rust")) {
            return findCategoryByName(categories, "计算机科学");
        }
        
        if (lowerName.contains("古代文学") || lowerName.contains("现当代文学") ||
            lowerName.contains("诗歌") || lowerName.contains("散文") ||
            lowerName.contains("小说") || lowerName.contains("戏剧文学") ||
            lowerName.contains("我与地坛") || lowerName.contains("活着") ||
            lowerName.contains("平凡的世界") || lowerName.contains("围城")) {
            return findCategoryByName(categories, "中国文学");
        }
        
        if (lowerName.contains("英美文学") || lowerName.contains("欧洲文学") ||
            lowerName.contains("日本文学") || lowerName.contains("俄罗斯文学") ||
            lowerName.contains("拉美文学") || lowerName.contains("莎士比亚") ||
            lowerName.contains("村上春树") || lowerName.contains("托尔斯泰")) {
            return findCategoryByName(categories, "外国文学");
        }
        
        if (lowerName.contains("文学理论") || lowerName.contains("文艺学") ||
            lowerName.contains("文学批评") || lowerName.contains("文学研究")) {
            return findCategoryByName(categories, "文学理论");
        }
        
        if (lowerName.contains("语言学") || lowerName.contains("语法") ||
            lowerName.contains("音韵") || lowerName.contains("词汇学")) {
            return findCategoryByName(categories, "语言学");
        }
        
        if (lowerName.contains("电影史") || lowerName.contains("电影理论") ||
            lowerName.contains("导演") || lowerName.contains("编剧") ||
            lowerName.contains("摄影") || lowerName.contains("剪辑") ||
            lowerName.contains("特效")) {
            return findCategoryByName(categories, "电影");
        }
        
        if (lowerName.contains("动画制作") || lowerName.contains("二维动画") ||
            lowerName.contains("三维动画") || lowerName.contains("动画理论")) {
            return findCategoryByName(categories, "动画");
        }
        
        if (lowerName.contains("纪录片")) {
            return findCategoryByName(categories, "纪录片");
        }
        
        if (lowerName.contains("电视剧")) {
            return findCategoryByName(categories, "电视剧");
        }
        
        if (lowerName.contains("课件") || lowerName.contains("ppt")) {
            return findCategoryByName(categories, "课件");
        }
        
        if (lowerName.contains("教案") || lowerName.contains("教学设计")) {
            return findCategoryByName(categories, "教案");
        }
        
        if (lowerName.contains("试卷") || lowerName.contains("考试") ||
            lowerName.contains("习题") || lowerName.contains("试题")) {
            return findCategoryByName(categories, "试题库");
        }
        
        if (lowerName.contains("实验") || lowerName.contains("实践") ||
            lowerName.contains("实训")) {
            return findCategoryByName(categories, "实验指导");
        }
        
        if (lowerName.contains("论文") || lowerName.contains("研究") ||
            lowerName.contains("毕业设计")) {
            return findCategoryByName(categories, "学术论文");
        }
        
        if (lowerName.contains("视频") || lowerName.contains("录像")) {
            return findCategoryByName(categories, "教学视频");
        }
        
        if (lowerName.contains("教材") || lowerName.contains("课本")) {
            return findCategoryByName(categories, "教材");
        }
        
        return null;
    }

    private MaterialCategory findCategoryByName(List<MaterialCategory> categories, String name) {
        for (MaterialCategory category : categories) {
            if (category.getCategoryName().equals(name)) {
                return category;
            }
            
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                MaterialCategory found = findCategoryByName(category.getChildren(), name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private Long searchCategory(MaterialCategory category, String categoryName) {
        if (category.getCategoryName().equals(categoryName)) {
            return category.getId();
        }
        
        if (category.getChildren() != null) {
            for (MaterialCategory child : category.getChildren()) {
                Long foundId = searchCategory(child, categoryName);
                if (foundId != null) {
                    return foundId;
                }
            }
        }
        
        return null;
    }
}
