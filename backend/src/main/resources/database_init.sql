-- 删除已存在的数据库
DROP DATABASE IF EXISTS teaching_material;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS teaching_material DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE teaching_material;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role_type VARCHAR(20) NOT NULL COMMENT '角色类型：TEACHER/STUDENT',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    description VARCHAR(200) COMMENT '权限描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 资料分类表
CREATE TABLE IF NOT EXISTS material_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示根分类',
    level INT DEFAULT 1 COMMENT '分类层级',
    path VARCHAR(500) COMMENT '分类路径',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料分类表';

-- 教学资料表
CREATE TABLE IF NOT EXISTS teaching_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '资料标题',
    description TEXT COMMENT '资料描述',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    object_name VARCHAR(500) COMMENT '对象名称',
    file_size BIGINT COMMENT '文件大小（字节）',
    upload_user BIGINT NOT NULL COMMENT '上传用户ID',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    ai_category VARCHAR(100) COMMENT 'AI分类（预留）',
    is_private TINYINT DEFAULT 0 COMMENT '是否为个人文件：0-公开 1-私密',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_category_id (category_id),
    INDEX idx_upload_user (upload_user),
    INDEX idx_is_private (is_private)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学资料表';

-- 下载记录表
CREATE TABLE IF NOT EXISTS download_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '资料ID',
    user_id BIGINT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    real_name VARCHAR(50) COMMENT '真实姓名',
    material_title VARCHAR(200) COMMENT '资料标题',
    download_ip VARCHAR(50) COMMENT '下载IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_material_id (material_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载记录表';

-- 分享记录表（新增）
CREATE TABLE IF NOT EXISTS shared_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '资料ID',
    share_code VARCHAR(32) UNIQUE NOT NULL COMMENT '分享码',
    share_password VARCHAR(20) COMMENT '访问密码（可选）',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    expire_time DATETIME COMMENT '过期时间',
    max_downloads INT DEFAULT -1 COMMENT '最大下载次数，-1表示无限制',
    current_downloads INT DEFAULT 0 COMMENT '当前下载次数',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效 0-失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_share_code (share_code),
    INDEX idx_material_id (material_id),
    INDEX idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享记录表';

-- 初始化角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('管理员', 'ADMIN', '系统管理员'),
('教师', 'TEACHER', '教师角色'),
('学生', 'STUDENT', '学生角色');

-- 初始化权限数据
INSERT INTO sys_permission (permission_name, permission_code, description) VALUES
('用户管理', 'user:manage', '用户管理权限'),
('角色管理', 'role:manage', '角色管理权限'),
('权限管理', 'permission:manage', '权限管理权限'),
('资料管理', 'material:manage', '资料管理权限'),
('资料查看', 'material:view', '资料查看权限'),
('资料上传', 'material:upload', '资料上传权限'),
('资料下载', 'material:download', '资料下载权限'),
('分类管理', 'category:manage', '分类管理权限'),
('下载记录', 'download:record', '下载记录查看权限'),
('分享管理', 'share:manage', '分享管理权限');

-- 初始化角色权限关联
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 10),
(3, 5), (3, 7);

-- 初始化管理员用户
INSERT INTO sys_user (username, password, email, phone, real_name, role_type) VALUES
('admin', 'admin', 'admin@example.com', '13800138000', '系统管理员', 'ADMIN');

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 初始化分类数据
INSERT INTO material_category (category_name, parent_id, level, path, sort) VALUES
('计算机科学', 0, 1, '/计算机科学', 1),
('数学', 0, 1, '/数学', 2),
('物理', 0, 1, '/物理', 3),
('数据结构', 1, 2, '/计算机科学/数据结构', 1),
('算法', 1, 2, '/计算机科学/算法', 2),
('数据库', 1, 2, '/计算机科学/数据库', 3),
('高等数学', 2, 2, '/数学/高等数学', 1),
('线性代数', 2, 2, '/数学/线性代数', 2);
