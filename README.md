# ServiceHub Backend

ServiceHub 是面向个人项目的后端基础服务平台。当前版本使用 Java 21、Spring Boot 3.x 的 Maven 单体多模块架构，提供管理员登录、服务 Token（凭证）管理、图片上传和短链能力。

## 功能特性

- **管理员认证**：账号密码登录，签发 30 天有效的访问凭证；剩余有效期不足 20 天时自动在响应头 `X-Renewed-Token` 下发新凭证实现滑动续期。
- **凭证管理**：创建、启用/禁用、删除服务 Token，支持自定义有效期。
- **调用记录**：开放接口的每次 Token 调用都会落库，管理端可分页查看并按服务类型筛选。
- **图片上传**：仅允许上传 JPG、PNG、GIF、WEBP 图片，单文件最大 10MB，存储到腾讯 COS 并记录文件信息。
- **内容去重**：上传前按 SHA-256 内容哈希查重，重复图片直接返回已有记录，不再重复存储。
- **开放文件接口**：第三方服务可使用 FILEHUB 类型 Token 调用图片查询、上传和删除接口。
- **短链服务（LinkHub）**：长链接转短码跳转，支持自定义短码和有效期；跳转记录来源（Referer 域名）与设备（桌面/移动/爬虫），统计接口返回总数、按天趋势、来源排行和设备分布；第三方服务可使用 LINKHUB 类型 Token 调用创建和查询接口。
- **健康检查**：`GET /api/health` 探测服务与数据库连通性。
- **接口文档**：内置 springdoc Swagger，浏览器打开 `/swagger-ui/index.html` 在线查看和调试。

## 技术栈

Java 21 · Spring Boot 3.4 · Maven · MyBatis-Plus · Flyway · MySQL 8 · 腾讯 COS · springdoc-openapi

## 模块结构

```text
servicehub
├── servicehub-common    # 统一响应、异常、常量和公共工具
├── servicehub-authhub   # Token 管理、Token 类型校验
├── servicehub-filehub   # 图片上传、COS 存储、文件记录
├── servicehub-linkhub   # 短链创建、跳转、访问统计
└── servicehub-admin     # 管理后台 API 和应用启动模块
```

依赖方向为 `admin → authhub → common`、`filehub → authhub → common`。

## 本地开发

要求：Java 21、Maven 3.9+、Docker（用于启动 MySQL）。

1. 启动 MySQL：

   ```bash
   docker compose up -d
   ```

2. 在项目根目录手动创建 `.env`（已被 Git 忽略，建议执行 `chmod 600 .env`）：

   ```dotenv
   MYSQL_ROOT_PASSWORD=你的MySQL密码
   DB_URL=jdbc:mysql://localhost:3306/servicehub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=root
   DB_PASSWORD=你的MySQL密码
   SERVICEHUB_ADMIN_USERNAME=hirongbao
   SERVICEHUB_ADMIN_PASSWORD=你的管理后台密码
   COS_SECRET_ID=你的腾讯云SecretId
   COS_SECRET_KEY=你的腾讯云SecretKey
   COS_REGION=ap-shanghai
   COS_BUCKET=你的Bucket名称
   COS_PUBLIC_URL_ENABLED=true
   ```

3. 启动应用（默认端口 `8080`，时区 `Asia/Shanghai`，启动时 Flyway 自动执行数据库迁移，见 `servicehub-admin/src/main/resources/db/migration/`）：

   ```bash
   mvn -f servicehub-admin/pom.xml spring-boot:run
   ```

   注意：该命令从本地 Maven 仓库（`~/.m2`）读取兄弟模块的快照 jar。修改了子模块代码后，需要先安装再启动，否则会运行旧代码：

   ```bash
   mvn -pl servicehub-admin -am install -DskipTests
   ```

   或打包后运行（始终使用最新代码）：

   ```bash
   mvn -pl servicehub-admin -am package -DskipTests
   java -jar servicehub-admin/target/servicehub-admin-*.jar
   ```

## WSL 环境安装（Ubuntu）

从零搭建 WSL2 Ubuntu 开发环境时，可参考以下步骤（已装好环境的可跳过）。

安装 Docker Engine 和 Compose 插件：

```bash
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc" | sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo groupadd --force docker && sudo usermod -aG docker "$USER"   # 需重新登录生效
```

安装 JDK 21、Maven 3.9（用户目录）和 Node.js 20：

```bash
sudo apt-get install -y openjdk-21-jdk curl ca-certificates
sudo apt-get install -y mysql-client   # 可选，便于手动连库排查

MAVEN_VERSION=3.9.11
mkdir -p ~/.local/opt ~/.local/bin
curl -fL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
  | tar -xz -C ~/.local/opt
ln -sfn ~/.local/opt/apache-maven-${MAVEN_VERSION}/bin/mvn ~/.local/bin/mvn

NODE_VERSION=20.20.2
curl -fL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.xz" \
  | tar -xJ -C ~/.local/opt
ln -sfn ~/.local/opt/node-v${NODE_VERSION}/bin/node ~/.local/bin/node
ln -sfn ~/.local/opt/node-v${NODE_VERSION}/bin/npm ~/.local/bin/npm
```

MySQL 使用 Docker 容器运行即可，无需在 WSL 主机安装 MySQL。

## 环境变量

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | Docker MySQL root 密码 | 无（必填） |
| `DB_URL` | 数据库连接串 | `jdbc:mysql://localhost:3306/servicehub?...` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | 空 |
| `SERVICEHUB_ADMIN_USERNAME` | 管理员账号 | `hirongbao` |
| `SERVICEHUB_ADMIN_PASSWORD` | 管理员密码 | 空 |
| `SERVICEHUB_AUTH_SECRET` | 登录凭证签名密钥，留空则每次重启后需重新登录 | 空 |
| `COS_SECRET_ID` / `COS_SECRET_KEY` | 腾讯云密钥 | 空 |
| `COS_REGION` | COS 地域 | `ap-shanghai` |
| `COS_BUCKET` | COS Bucket 名称 | 空 |
| `COS_PUBLIC_URL_ENABLED` | 是否生成公开访问 URL | `true` |
| `SERVICEHUB_LINK_BASE_URL` | 短链展示用的基础地址（如 `https://s.example.com`），留空按请求 Host 推断 | 空 |

## API 一览

统一响应格式为 `{ "code": 0, "data": ..., "message": "success" }`，`code=0` 表示成功。

### 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/login` | 管理员登录，返回 `{ token, username }` |
| POST | `/api/admin/logout` | 注销 |
| GET | `/api/overview` | 概览聚合统计 |
| GET | `/api/health` | 健康检查（无需登录），数据库异常时返回 503 |
| GET | `/api/tokens` | 查询凭证列表 |
| POST | `/api/tokens` | 创建凭证，参数 `{ tokenName, tokenType, validDays }` |
| POST | `/api/tokens/{id}/status` | 启用/禁用凭证，参数 `{ status: 0 \| 1 }` |
| DELETE | `/api/tokens/{id}` | 删除凭证 |
| GET | `/api/usage` | 分页查询 Token 调用记录，参数 `hub`（all/FILEHUB/LINKHUB）、`page`、`size` |
| GET | `/api/files` | 查询文件列表 |
| POST | `/api/files/upload` | 上传图片，`multipart` 字段名 `file` |
| DELETE | `/api/files/{id}` | 删除文件（同时删除 COS 对象） |
| GET | `/api/links` | 查询短链列表（含点击统计） |
| POST | `/api/links` | 创建短链，参数 `{ targetUrl, code?, remark?, validDays? }` |
| POST | `/api/links/{id}/status` | 启用/禁用短链，参数 `{ status: 0 \| 1 }` |
| DELETE | `/api/links/{id}` | 删除短链及访问记录 |
| GET | `/api/links/{id}/stats` | 查询短链点击统计：`total` 总数、`daily` 近 30 天按天、`sources` 来源排行、`devices` 设备分布 |

### 开放文件接口（FILEHUB Token 鉴权）

通过请求头 `X-Service-Token` 或 `Authorization: Bearer <token>` 携带 FILEHUB 类型 Token：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/filehub` | 查询文件列表 |
| POST | `/api/filehub/upload` | 上传图片 |
| DELETE | `/api/filehub/{id}` | 删除图片 |

### 开放短链接口（LINKHUB Token 鉴权）

通过请求头 `X-Service-Token` 或 `Authorization: Bearer <token>` 携带 LINKHUB 类型 Token：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/linkhub/links` | 创建短链 |
| GET | `/api/linkhub/links/{code}` | 查询短链详情和统计 |

### 短链跳转

`GET /s/{code}`：匿名可访问，302 跳转到目标地址；不存在、已禁用或已过期返回 404 页面。每次有效跳转记一次点击统计。

## v1 边界

Redis、复杂权限、多用户、多租户、CDN、临时签名 URL 和异常补偿暂不实现。

## 数据库迁移（Flyway）

表结构由 Flyway 版本化脚本管理，位于 `servicehub-admin/src/main/resources/db/migration/`：

| 版本 | 内容 |
| --- | --- |
| V1 | 初始五张表（service_token、file_record、token_usage_log、short_link、link_visit_log） |
| V2 | file_record 补充 content_hash 列与唯一索引 |
| V3 | link_visit_log 补充 referer、user_agent 列 |

存量库首次启动时自动打基线（baseline-version=2），跳过 V1/V2 只应用之后的增量脚本；全新库从 V1 开始完整执行。新增表结构变更时，添加 `V<N>__描述.sql` 脚本即可，不要改动已发布的历史脚本。

## MySQL 备份（systemd timer）

本项目在 WSL 中配置了每日自动备份：`~/projects/mysql-backup.sh` 通过 `docker exec` 调用容器内 `mysqldump`（`--single-transaction` 一致性快照、不锁表），gzip 压缩后写入 `~/backups/servicehub/`，文件权限 600，保留最近 14 份。

- 定时器：`~/.config/systemd/user/servicehub-backup.timer`，每天 03:30 执行（随 WSL 开机自启，Persistent 补偿错过的触发）
- 手动执行一次备份：`systemctl --user start servicehub-backup.service`
- 查看备份日志：`journalctl --user -u servicehub-backup -f`
- 恢复数据：`zcat ~/backups/servicehub/servicehub-<时间>.sql.gz | docker exec -i servicehub-mysql mysql -uroot -p"密码" servicehub`

## 日志

应用日志写入项目根目录的 `logs/`（已被 Git 忽略），文件名按实例端口区分，互不覆盖：

| 文件 | 说明 |
| --- | --- |
| `logs/servicehub-8080.log` | systemd 自动启动的正式实例 |
| `logs/servicehub-<port>.log` | 手动起的其他实例（如测试实例） |

滚动策略：每天归档为 `*.log.<日期>.<序号>.log.gz`，归档保留 **7 天**后自动清理；另有单文件 10MB、总量 200MB 的上限保护。

```bash
# 实时跟踪正式实例日志
tail -f logs/servicehub-8080.log
```

每个 HTTP 请求会由 `RequestLogInterceptor` 输出一行访问日志，包含方法、接口、状态码、耗时和调用方身份（管理端为登录用户名，开放接口为使用的凭证名称，`-` 表示匿名/未登录）：

```text
POST /api/admin/login   200 140ms user=-          token=-
GET  /api/links         200  68ms user=hirongbao  token=-
GET  /s/mydoc           302  23ms user=-          token=-
```

### Web 日志查看器（Log Viewer）

本机 8111 端口常驻了一个 [log-viewer](https://github.com/sevdokimov/log-viewer)（systemd 用户服务 `log-viewer`），并经 Vite 代理挂在 `/logs-ui` 路径下。浏览器打开即看：

- 直达后端正式日志：`http://localhost:5173/logs-ui/log?log=backend`
- 管理页（文件选择）：`http://localhost:5173/logs-ui/`

> 直连 `http://localhost:8111/logs-ui/` 仅在 WSL 内可用——WSL2 的 localhost 端口转发对服务启动后才新增的端口不生效（重启 WSL 后 8111 直连才会注册）。

支持实时刷新、日志级别过滤、关键字搜索和异常堆栈高亮。服务管理：

```bash
systemctl --user status log-viewer      # 查看状态
systemctl --user restart log-viewer     # 重启
```

程序位置 `~/.local/opt/log-viewer/log-viewer-1.0.11/`，配置在同目录 `config.conf`（context-path 为 `/logs-ui`，已配置 `backend` 短路径、只绑定 localhost、关闭统计外发）。

## 自动部署（systemd timer）

本项目在 WSL 中配置了基于 systemd user timer 的简易 CI/CD：每 2 分钟检查一次 GitHub 上 `main` 分支的最新 commit（fetch 带 60 秒超时，网络挂起不会堵住部署管道），若与上次已部署的 commit 不同，则自动重新打包（`mvn -pl servicehub-admin -am package -DskipTests`）并重启 `servicehub-backend` 服务。

日常开发中**推送到 GitHub 后无需手动重启服务**，最迟约 2 分钟后新代码自动生效（本地提交但未推送不会触发部署）。

组成：

- 部署脚本：`~/projects/deploy-check.sh`（对比 `origin/main` 与 `~/.local/state/servicehub-deploy/backend` 中记录的 commit）
- 定时器：`~/.config/systemd/user/servicehub-deploy.timer`，随 WSL 开机自启
- 查看部署日志：`journalctl --user -u servicehub-deploy -f`
- 临时停用自动部署：`systemctl --user disable --now servicehub-deploy.timer`

注意：systemd 服务与手动运行的实例不能同时启动（端口 `8080` 冲突）。若推送代码时手动实例还在运行，自动部署重启的 systemd 后端会因端口占用反复重试，直到手动实例停止。建议平时让 systemd 服务接管后端，需要临时调试再手动启动，用完即停。
