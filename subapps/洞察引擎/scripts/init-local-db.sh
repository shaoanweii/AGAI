#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-voc-mysql}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"

mysql_exec() {
  docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
}

import_sql() {
  local database="$1"
  local file="$2"
  echo "[db:init] importing ${file} -> ${database}"
  mysql_exec "$database" < "$ROOT_DIR/$file"
}

import_sql_force() {
  local database="$1"
  local file="$2"
  echo "[db:init] importing ${file} -> ${database} (force)"
  mysql_exec --force "$database" < "$ROOT_DIR/$file"
}

import_sql_force_rewrite_voc_schema() {
  local database="$1"
  local file="$2"
  echo "[db:init] importing ${file} -> ${database} (force, rewrite voc. prefix)"
  sed "s/voc\\./${database}./g" "$ROOT_DIR/$file" \
    | awk '/^public static void main/ { skip=1; next } skip && /^}/ { skip=0; next } !skip { print }' \
    | mysql_exec --force "$database"
}

import_sql_rewrite_voc_schema() {
  local database="$1"
  local file="$2"
  echo "[db:init] importing ${file} -> ${database} (rewrite voc. prefix)"
  sed "s/voc\\./${database}./g" "$ROOT_DIR/$file" | mysql_exec
}

echo "[db:init] recreating local databases"
mysql_exec <<SQL
SET GLOBAL sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
DROP DATABASE IF EXISTS vdp_ms_be;
DROP DATABASE IF EXISTS vdp_ms_td;
CREATE DATABASE IF NOT EXISTS vdp_ms_be DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vdp_ms_td DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SQL

import_sql "vdp_ms_be" "后端/voc-service-security/voc-security-server/schema/mysql_create.sql"
import_sql_rewrite_voc_schema "vdp_ms_be" "后端/voc-service-security/voc-security-server/schema/mysql_init.sql"
mysql_exec "vdp_ms_be" <<SQL
ALTER TABLE sys_users ADD COLUMN employee_id varchar(50) DEFAULT NULL COMMENT '员工编号';
ALTER TABLE sys_credentials ADD COLUMN admin tinyint(1) DEFAULT 0 COMMENT '是否管理员';
UPDATE sys_users
SET client_id = '764547797eb2e192763f5334028d49c9'
WHERE id = '1' AND username = 'admin';
UPDATE sys_credentials
SET credential = 'YoUlTZ0Ag7u0QEvXw6obnAQ3r79o9TELTicYL1wjLD7C8bw3SA0ASVgiLzsjATHg'
WHERE identifier = 'admin' AND app_id = 'insights' AND identity_type = 'base';
SQL

for database in vdp_ms_be vdp_ms_td; do
  import_sql_force "$database" "后端/voc-service-insights/voc-insights-impl/schema/mysql_create.sql"
  mysql_exec "$database" <<SQL
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
CREATE TABLE IF NOT EXISTS ins_customer_permission (
  id varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  client_id varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  permission_id varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  button_permission int DEFAULT NULL,
  permission_type int DEFAULT NULL,
  create_user varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_client_permission (client_id, permission_id, permission_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地最小客户权限表';
SQL
  import_sql_force_rewrite_voc_schema "$database" "后端/voc-service-insights/voc-insights-impl/schema/mysql_permission_create_step1.sql"
  import_sql_force "$database" "后端/voc-service-insights/voc-insights-impl/schema/mysql_customer_create_step2.sql"
  import_sql_force "$database" "scripts/dev-seed-permissions.sql"
done

echo "[db:init] done"
