# 高校教学资料管理系统

基于 Spring Boot 2.7.18 + Vue 3 + Element Plus 的前后端分离教学资料管理系统。

## 技术栈

### 后端
- Spring Boot 2.7.18
- JDK 1.8
- MySQL 8.0
- MyBatis-Plus 3.5.3.1
- Spring Security + JWT
- MinIO 8.5.2 对象存储
- Maven 构建

### 前端
- Vue 3
- Element Plus
- Vue Router
- Pinia
- Axios
- Vite

## 系统功能

### 1. 用户认证
- 用户注册（教师/学生）
- 用户登录
- JWT Token 认证
- BCrypt 密码加密

### 2. RBAC 权限管理
- 用户管理 CRUD
- 角色管理 CRUD
- 权限管理 CRUD
- 用户角色关联
- 角色权限关联
- 基于 @PreAuthorize 的接口权限控制

### 3. 教学资料管理
- 资料分类管理（树形结构）
- 教学资料 CRUD
- 文件上传（支持 100MB 大文件）
- 文件下载（支持中文文件名）
- 记录下载次数

### 4. 文件存储
- MinIO 对象存储
- 自动创建 Bucket
- 文件流式上传
- 生成预签名 URL

### 5. 下载记录
- 记录用户下载行为
- 记录下载 IP
- 下载历史查询
- 下载次数统计

## 快速开始

### 前置要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- MinIO Server

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 2. 配置 MinIO

```bash
# 启动 MinIO (Docker)
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

### 3. 后端启动

```bash
cd backend

# 修改 application.yml 中的数据库和 MinIO 配置
# src/main/resources/application.yml

# 使用 Maven 构建
mvn clean install

# 运行项目
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:3000 启动

## 默认账号

系统初始化时会创建以下角色：
- 管理员 (ADMIN)
- 教师 (TEACHER)
- 学生 (STUDENT)

您需要通过注册功能创建用户账号。

## 项目结构

### 后端结构
```
backend/
├── src/main/java/com/edu/material/
│   ├── common/          # 通用类
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── mapper/          # MyBatis Mapper
│   ├── security/        # 安全配置
│   ├── service/         # 服务层
│   ├── util/            # 工具类
│   └── vo/              # 视图对象
└── src/main/resources/
    ├── application.yml  # 应用配置
    └── schema.sql       # 数据库脚本
```

### 前端结构
```
frontend/
├── src/
│   ├── api/             # API 接口
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia 状态管理
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

## API 接口

### 认证接口
- POST /api/auth/login - 用户登录
- POST /api/auth/register - 用户注册
- GET /api/auth/info - 获取用户信息

### 资料管理接口
- GET /api/material/list - 获取资料列表
- POST /api/material/upload - 上传资料
- GET /api/material/download/{id} - 下载资料
- DELETE /api/material/{id} - 删除资料

### 分类管理接口
- GET /api/category/tree - 获取分类树
- POST /api/category - 创建分类
- PUT /api/category/{id} - 更新分类
- DELETE /api/category/{id} - 删除分类

### 用户管理接口
- GET /api/user/list - 获取用户列表
- POST /api/user - 创建用户
- PUT /api/user/{id} - 更新用户
- DELETE /api/user/{id} - 删除用户
- POST /api/user/{userId}/role/{roleId} - 分配角色

### 角色管理接口
- GET /api/role/list - 获取角色列表
- POST /api/role - 创建角色
- PUT /api/role/{id} - 更新角色
- DELETE /api/role/{id} - 删除角色

### 权限管理接口
- GET /api/permission/list - 获取权限列表
- POST /api/permission - 创建权限
- PUT /api/permission/{id} - 更新权限
- DELETE /api/permission/{id} - 删除权限
- POST /api/permission/role/{roleId}/assign - 分配权限

### 下载记录接口
- GET /api/download/records - 获取下载记录
- GET /api/download/count/material/{materialId} - 获取资料下载次数
- GET /api/download/count/user/{userId} - 获取用户下载次数

## 安全机制

- Spring Security 无状态认证
- JWT Token 认证
- @PreAuthorize 方法级权限控制
- 登录、注册、下载接口允许匿名访问
- CORS 跨域配置

## 开发说明

### 在 IntelliJ IDEA 中运行后端

1. 导入项目：File -> Open -> 选择 backend 目录
2. 等待 Maven 依赖下载完成
3. 修改 `src/main/resources/application.yml` 中的数据库和 MinIO 配置
4. 运行 `TeachingMaterialApplication` 类

### 在 IntelliJ IDEA 中运行前端

1. 安装 Node.js 插件
2. 打开 Terminal，进入 frontend 目录
3. 运行 `npm install` 安装依赖
4. 运行 `npm run dev` 启动开发服务器

## 注意事项

1. 确保 MySQL 和 MinIO 服务已启动
2. 首次运行前需要执行数据库初始化脚本
3. MinIO 默认访问地址：http://localhost:9000
4. 前端开发服务器默认端口：3000
5. 后端服务默认端口：8080

## 许可证

MIT License