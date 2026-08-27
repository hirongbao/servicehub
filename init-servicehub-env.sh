#!/usr/bin/env bash

set -Eeuo pipefail

env_file="$(cd "$(dirname "$0")" && pwd)/.env"

echo "此脚本只会将配置保存到：$env_file"
echo "文件权限会设置为 600，不会打印密钥，也不会提交到 Git。"
echo

read -rsp "MySQL root 密码: " mysql_password
printf '\n'
read -rsp "新的管理后台密码: " admin_password
printf '\n'
read -rp "腾讯云 COS SecretId: " cos_secret_id
read -rsp "腾讯云 COS SecretKey: " cos_secret_key
printf '\n'

umask 077
{
  printf '%s\n' 'MYSQL_ROOT_PASSWORD='"$mysql_password"
  printf '%s\n' 'DB_URL=jdbc:mysql://localhost:3306/servicehub?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
  printf '%s\n' 'DB_USERNAME=root'
  printf '%s\n' 'DB_PASSWORD='"$mysql_password"
  printf '%s\n' 'SERVICEHUB_ADMIN_USERNAME=hirongbao'
  printf '%s\n' 'SERVICEHUB_ADMIN_PASSWORD='"$admin_password"
  printf '%s\n' 'COS_SECRET_ID='"$cos_secret_id"
  printf '%s\n' 'COS_SECRET_KEY='"$cos_secret_key"
  printf '%s\n' 'COS_REGION=ap-shanghai'
  printf '%s\n' 'COS_BUCKET=hirongbao-1321185798'
  printf '%s\n' 'COS_PUBLIC_URL_ENABLED=true'
} > "$env_file"
chmod 600 "$env_file"

echo
echo "环境变量已持久化到项目本地 .env。"
echo "以后可以直接执行："
echo "  cd $(dirname "$env_file")"
echo "  docker compose up -d"
echo "  mvn -f servicehub-admin/pom.xml spring-boot:run"
