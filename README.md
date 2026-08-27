# ServiceHub Backend

ServiceHub 是面向个人项目的后端基础服务平台。v1 使用 Java 21、Spring Boot 3.x、Maven 单体多模块架构，提供管理员登录、Token 管理和图片上传能力。

## 模块

- `servicehub-common`：统一响应、异常、常量和公共工具。
- `servicehub-authhub`：Token 管理、Token 类型校验和统一鉴权拦截器。
- `servicehub-filehub`：图片上传、COS 存储和文件记录。
- `servicehub-admin`：管理后台 API 和应用启动模块。

依赖方向为 `admin → authhub → common`、`filehub → authhub → common`。

## 本地开发

要求：Java 21、Maven 3.9+、MySQL 8、腾讯 COS 配置。

首次使用时执行 `bash init-servicehub-env.sh` 持久化本地环境变量。该脚本生成的 `.env` 已被 Git 忽略。

```bash
mvn -pl servicehub-admin -am package -DskipTests
mvn -f servicehub-admin/pom.xml spring-boot:run
```

默认端口为 `8080`，时区为 `Asia/Shanghai`。管理员默认账号为 `hirongbao`，密码为 `123456`。

## v1 规划

- 管理员登录和 Token CRUD
- `FILEHUB` Token 统一鉴权
- 仅允许图片上传
- COS URL 默认永久公开

Redis、复杂权限、多用户、多租户、CDN、临时签名 URL 和异常补偿暂不实现。
