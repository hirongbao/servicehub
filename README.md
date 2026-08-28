# ServiceHub Backend

ServiceHub 是面向个人项目的后端基础服务平台。当前版本使用 Java 21、Spring Boot 3.x 的 Maven 单体多模块架构，提供管理员登录、服务 Token（凭证）管理和图片上传能力。

## 功能特性

- **管理员认证**：账号密码登录，签发访问凭证。
- **凭证管理**：创建、启用/禁用、删除服务 Token，支持自定义有效期。
- **图片上传**：仅允许上传 JPG、PNG、GIF、WEBP 图片，单文件最大 10MB，存储到腾讯 COS 并记录文件信息。
- **开放文件接口**：第三方服务可使用 FILEHUB 类型 Token 调用图片查询、上传和删除接口。

## 技术栈

Java 21 · Spring Boot 3.4 · Maven · MyBatis-Plus · MySQL 8 · 腾讯 COS

## 模块结构

```text
servicehub
├── servicehub-common    # 统一响应、异常、常量和公共工具
├── servicehub-authhub   # Token 管理、Token 类型校验
├── servicehub-filehub   # 图片上传、COS 存储、文件记录
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

3. 启动应用（默认端口 `8080`，时区 `Asia/Shanghai`，启动时自动执行 `schema.sql` 建表）：

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
| `COS_SECRET_ID` / `COS_SECRET_KEY` | 腾讯云密钥 | 空 |
| `COS_REGION` | COS 地域 | `ap-shanghai` |
| `COS_BUCKET` | COS Bucket 名称 | 空 |
| `COS_PUBLIC_URL_ENABLED` | 是否生成公开访问 URL | `true` |

## API 一览

统一响应格式为 `{ "code": 0, "data": ..., "message": "success" }`，`code=0` 表示成功。

### 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/login` | 管理员登录，返回 `{ token, username }` |
| POST | `/api/admin/logout` | 注销 |
| GET | `/api/tokens` | 查询凭证列表 |
| POST | `/api/tokens` | 创建凭证，参数 `{ tokenName, tokenType, validDays }` |
| POST | `/api/tokens/{id}/status` | 启用/禁用凭证，参数 `{ status: 0 \| 1 }` |
| DELETE | `/api/tokens/{id}` | 删除凭证 |
| GET | `/api/files` | 查询文件列表 |
| POST | `/api/files/upload` | 上传图片，`multipart` 字段名 `file` |
| DELETE | `/api/files/{id}` | 删除文件（同时删除 COS 对象） |

### 开放文件接口（FILEHUB Token 鉴权）

通过请求头 `X-Service-Token` 或 `Authorization: Bearer <token>` 携带 FILEHUB 类型 Token：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/filehub` | 查询文件列表 |
| POST | `/api/filehub/upload` | 上传图片 |
| DELETE | `/api/filehub/{id}` | 删除图片 |

## v1 边界

Redis、复杂权限、多用户、多租户、CDN、临时签名 URL 和异常补偿暂不实现。
