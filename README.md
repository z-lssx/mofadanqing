# 项目说明

本项目是一个前后端分离的电商/定制平台系统。

## 目录结构

- **frontend/**: Vue.js 前端项目
  - 使用 Vite 构建
  - 运行: `cd frontend` -> `npm install` -> `npm run dev`
- **backend/**: Spring Boot 后端项目
  - 使用 Maven 构建
  - 运行: `cd backend` -> `mvn spring-boot:run`

## 开发指南

1. **数据库**: 请确保本地 MySQL 服务已启动，并导入 `backend/src/main/resources/sql` 下的 SQL 脚本。
2. **启动后端**: 在 `backend` 目录下运行 Spring Boot 应用。
3. **启动前端**: 在 `frontend` 目录下安装依赖并启动开发服务器。
