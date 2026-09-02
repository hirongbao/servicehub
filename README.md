# ServiceHub Backend

ServiceHub 是基于 Java 21、Spring Boot 和 Maven 多模块架构的个人服务中台。

## 能力

- 管理员认证、Service Token、调用额度和使用记录
- 图片上传、内容去重与腾讯云 COS 存储
- 短链创建、跳转与访问统计
- hirongbao 个人网站资料、动态、媒体、分类、点赞、评论和在线人数
- 健康检查与 Swagger 接口文档

## 技术栈

Java 21 · Spring Boot 3.4 · Maven · MyBatis-Plus · Flyway · MySQL 8 · 腾讯云 COS

## 模块

```text
servicehub-common       公共响应、异常和工具
servicehub-authhub      管理员认证与 Service Token
servicehub-filehub      文件上传与 COS
servicehub-linkhub      短链与访问统计
servicehub-hirongbaohub 个人网站接口
servicehub-admin        应用启动与管理接口
```

## 本地开发

要求：Java 21、Maven 3.9+、Docker。准备好数据库和环境变量后执行：

```bash
mvn -pl servicehub-admin -am clean package -DskipTests
java -jar servicehub-admin/target/servicehub-admin-*.jar
```

默认端口为 `8080`。Flyway 会自动执行 `servicehub-admin/src/main/resources/db/migration/` 中的新迁移。

## 主要接口

统一响应格式为 `{ code, data, message }`，成功时 `code` 为 `0`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/health` | 健康检查 |
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/hirongbaohub/profile` | 公开站点资料 |
| GET | `/api/hirongbaohub/posts?category=notes` | 公开动态和分类筛选 |
| POST | `/api/hirongbaohub/heartbeat` | 在线人数心跳 |
| GET | `/swagger-ui/index.html` | Swagger UI |

## 部署

推送 `main` 后，GitHub Actions 构建并通过 SSH 调用服务器脚本：

```text
/opt/scripts/deploy-servicehub-backend.sh
```

生产环境 `.env` 位于服务器 `/opt/apps/servicehub/shared/.env`，不要提交到仓库。

## 约定

- 不提交密码、COS 密钥、认证密钥或生产 `.env`。
- 新数据库变更只能新增 `V<N>__描述.sql`，不要修改已发布迁移。
