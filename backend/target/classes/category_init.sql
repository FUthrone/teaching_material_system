USE teaching_material;

DELETE FROM material_category;

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('计算机科学', 0, 1, '/计算机科学', 1),
('文学', 0, 1, '/文学', 2),
('影视', 0, 1, '/影视', 3),
('资料类型', 0, 1, '/资料类型', 4);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('编程语言', 1, 2, '/计算机科学/编程语言', 1),
('人工智能', 1, 2, '/计算机科学/人工智能', 2),
('计算机基础', 1, 2, '/计算机科学/计算机基础', 3),
('软件开发', 1, 2, '/计算机科学/软件开发', 4),
('数据与算法', 1, 2, '/计算机科学/数据与算法', 5);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('Python', 5, 3, '/计算机科学/编程语言/Python', 1),
('Java', 5, 3, '/计算机科学/编程语言/Java', 2),
('C语言', 5, 3, '/计算机科学/编程语言/C语言', 3),
('C++', 5, 3, '/计算机科学/编程语言/C++', 4),
('JavaScript', 5, 3, '/计算机科学/编程语言/JavaScript', 5),
('Go', 5, 3, '/计算机科学/编程语言/Go', 6),
('Rust', 5, 3, '/计算机科学/编程语言/Rust', 7);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('机器学习', 6, 3, '/计算机科学/人工智能/机器学习', 1),
('深度学习', 6, 3, '/计算机科学/人工智能/深度学习', 2),
('计算机视觉', 6, 3, '/计算机科学/人工智能/计算机视觉', 3),
('自然语言处理', 6, 3, '/计算机科学/人工智能/自然语言处理', 4),
('大数据', 6, 3, '/计算机科学/人工智能/大数据', 5);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('数据结构', 9, 3, '/计算机科学/数据与算法/数据结构', 1),
('算法', 9, 3, '/计算机科学/数据与算法/算法', 2),
('数据库', 9, 3, '/计算机科学/数据与算法/数据库', 3),
('操作系统', 7, 3, '/计算机科学/计算机基础/操作系统', 1),
('计算机网络', 7, 3, '/计算机科学/计算机基础/计算机网络', 2),
('计算机组成原理', 7, 3, '/计算机科学/计算机基础/计算机组成原理', 3),
('编译原理', 7, 3, '/计算机科学/计算机基础/编译原理', 4);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('前端开发', 8, 3, '/计算机科学/软件开发/前端开发', 1),
('后端开发', 8, 3, '/计算机科学/软件开发/后端开发', 2),
('移动开发', 8, 3, '/计算机科学/软件开发/移动开发', 3),
('游戏开发', 8, 3, '/计算机科学/软件开发/游戏开发', 4),
('软件工程', 8, 3, '/计算机科学/软件开发/软件工程', 5);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('中国文学', 2, 2, '/文学/中国文学', 1),
('外国文学', 2, 2, '/文学/外国文学', 2),
('文学理论', 2, 2, '/文学/文学理论', 3),
('语言学', 2, 2, '/文学/语言学', 4);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('古代文学', 16, 3, '/文学/中国文学/古代文学', 1),
('现当代文学', 16, 3, '/文学/中国文学/现当代文学', 2),
('诗歌', 16, 3, '/文学/中国文学/诗歌', 3),
('散文', 16, 3, '/文学/中国文学/散文', 4),
('小说', 16, 3, '/文学/中国文学/小说', 5),
('戏剧文学', 16, 3, '/文学/中国文学/戏剧文学', 6);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('英美文学', 17, 3, '/文学/外国文学/英美文学', 1),
('欧洲文学', 17, 3, '/文学/外国文学/欧洲文学', 2),
('日本文学', 17, 3, '/文学/外国文学/日本文学', 3),
('俄罗斯文学', 17, 3, '/文学/外国文学/俄罗斯文学', 4),
('拉美文学', 17, 3, '/文学/外国文学/拉美文学', 5);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('电影', 3, 2, '/影视/电影', 1),
('电视剧', 3, 2, '/影视/电视剧', 2),
('纪录片', 3, 2, '/影视/纪录片', 3),
('动画', 3, 2, '/影视/动画', 4),
('影视理论', 3, 2, '/影视/影视理论', 5);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('电影史', 28, 3, '/影视/电影/电影史', 1),
('电影理论', 28, 3, '/影视/电影/电影理论', 2),
('导演艺术', 28, 3, '/影视/电影/导演艺术', 3),
('编剧', 28, 3, '/影视/电影/编剧', 4),
('摄影', 28, 3, '/影视/电影/摄影', 5),
('剪辑', 28, 3, '/影视/电影/剪辑', 6),
('特效', 28, 3, '/影视/电影/特效', 7);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('动画制作', 31, 3, '/影视/动画/动画制作', 1),
('二维动画', 31, 3, '/影视/动画/二维动画', 2),
('三维动画', 31, 3, '/影视/动画/三维动画', 3),
('动画理论', 31, 3, '/影视/动画/动画理论', 4);

INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('课件', 4, 2, '/资料类型/课件', 1),
('教案', 4, 2, '/资料类型/教案', 2),
('试题库', 4, 2, '/资料类型/试题库', 3),
('实验指导', 4, 2, '/资料类型/实验指导', 4),
('教学视频', 4, 2, '/资料类型/教学视频', 5),
('学术论文', 4, 2, '/资料类型/学术论文', 6),
('教材', 4, 2, '/资料类型/教材', 7);
