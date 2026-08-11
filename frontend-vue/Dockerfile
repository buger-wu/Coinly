# ============================================================
# Coinly 前端 Dockerfile（多阶段构建）
# Stage 1: Node 编译
# Stage 2: Nginx 运行时
# ============================================================

# ---- Stage 1: Build ----
FROM node:22-alpine AS builder
WORKDIR /build

# 复制 package.json 并安装依赖
COPY package.json pnpm-lock.yaml* package-lock.json* ./
RUN npm install

# 复制源码并构建
COPY . .
RUN npx vite build

# ---- Stage 2: Runtime ----
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
