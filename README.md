# Coinly - 轻量级个人记账应用

基于 Spring Boot 3.4 + Vue 3.5 的全栈个人记账应用，支持多账本、分类记账、统计报表、预算管控，可通过 Docker 一键部署。

## 技术栈

- **后端**：Java 17、Spring Boot 3.4、MyBatis-Plus、MySQL 8、Redis 7、RabbitMQ 3
- **前端**：Vue 3.5、TypeScript、Vite、Element Plus、ECharts
- **部署**：Docker、Docker Compose、Nginx

## 项目结构

```
Coinly/
├── docker-compose.yml          # 全栈 Docker 编排
├── .env.example                # 环境变量模板
├── backend-java/               # 后端（Maven 多模块）
│   ├── Dockerfile
│   └── docs/schema.sql         # 数据库初始化脚本
└── frontend-vue/               # 前端
    ├── Dockerfile
    └── nginx.conf
```

## 快速部署

### 1. 克隆项目

```bash
git clone <仓库地址>
cd Coinly
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`，修改默认敏感配置：

| 变量 | 建议 |
|------|------|
| `DB_PASSWORD` | 不要用默认 `123456` |
| `JWT_SECRET_KEY` | 使用 `openssl rand -base64 64` 生成 |
| `MQ_USERNAME` / `MQ_PASSWORD` | 不要用默认 `guest` |

### 3. 启动服务

```bash
docker-compose up -d --build
```

### 4. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8088 | 前端页面 |
| http://localhost:8080/api/swagger-ui.html | API 文档 |
| http://localhost:15672 | RabbitMQ 管理台 |

## 常用命令

```bash
# 启动
docker-compose up -d

# 停止
docker-compose down

# 重启并重新构建（代码变更后）
docker-compose up -d --build

# 停止并清空数据（重新初始化数据库）
docker-compose down -v

# 查看日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

## 注意事项

1. **不要提交 `.env` 文件到仓库**，`.gitignore` 已配置忽略。
2. 首次启动会自动执行 `backend-java/docs/schema.sql` 初始化数据库和默认分类。
3. MySQL 宿主机端口映射为 `3307`，避免与本地 MySQL 冲突。
4. 生产环境请关闭 Knife4j 文档、限制 Actuator 暴露端点，并修改所有默认密码。
