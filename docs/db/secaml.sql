-- 租户（可选）
CREATE TABLE IF NOT EXISTS sys_tenant (
                                          id            BIGINT UNSIGNED PRIMARY KEY,
                                          code          VARCHAR(64)  NOT NULL UNIQUE,
                                          name          VARCHAR(128) NOT NULL,
                                          status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用,0=禁用',
                                          created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                          updated_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 用户主档（不直接存登录密码）
CREATE TABLE IF NOT EXISTS user_user (
                                         id              BIGINT UNSIGNED PRIMARY KEY COMMENT '雪花/ULID',
                                         tenant_id       BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                         nickname        VARCHAR(64)  NOT NULL,
                                         avatar_url      VARCHAR(512),
                                         gender          TINYINT NOT NULL DEFAULT 0 COMMENT '0未知,1男,2女,9其他',
                                         timezone        VARCHAR(64),
                                         locale          VARCHAR(16)  DEFAULT 'zh_CN',
                                         status          TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0禁用,2锁定',
                                         token_version   INT NOT NULL DEFAULT 0 COMMENT '全局令牌版本,变更后可强制所有JWT失效',
                                         last_login_at   DATETIME(3),
                                         remark          VARCHAR(255),
                                         created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                         updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                         deleted         TINYINT(1) NOT NULL DEFAULT 0,
                                         version         BIGINT NOT NULL DEFAULT 0,
                                         KEY idx_tenant (tenant_id),
                                         KEY idx_status (status),
                                         KEY idx_last_login (last_login_at),
                                         KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 账号（支持用户名/邮箱/手机/OAuth 等多身份）
CREATE TABLE IF NOT EXISTS auth_account (
                                            id                 BIGINT UNSIGNED PRIMARY KEY,
                                            tenant_id          BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                            user_id            BIGINT UNSIGNED NOT NULL,
                                            identity_type      VARCHAR(32)  NOT NULL COMMENT 'username/email/phone/oauth:github/oauth:google/...',
                                            identifier         VARCHAR(255) NOT NULL COMMENT '用户名/邮箱/手机/三方openId等',
                                            credential_hash    VARCHAR(255)     NULL COMMENT '本地密码BCrypt哈希; OAuth为空',
                                            password_version   INT NOT NULL DEFAULT 0 COMMENT '用于密码变更使旧JWT失效',
                                            is_primary         TINYINT(1) NOT NULL DEFAULT 0,
                                            is_verified        TINYINT(1) NOT NULL DEFAULT 0,
                                            verified_at        DATETIME(3),
                                            failed_attempts    INT NOT NULL DEFAULT 0,
                                            lock_until         DATETIME(3),
                                            last_login_at      DATETIME(3),
                                            meta_json          JSON,
                                            status             TINYINT NOT NULL DEFAULT 1 COMMENT '1正常,0禁用,2锁定',
                                            created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                            updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                            deleted            TINYINT(1) NOT NULL DEFAULT 0,
                                            version            BIGINT NOT NULL DEFAULT 0,
                                            UNIQUE KEY uk_tenant_ident (tenant_id, identity_type, identifier),
                                            KEY idx_user (user_id),
                                            CONSTRAINT fk_acc_user FOREIGN KEY (user_id) REFERENCES user_user(id)
                                                ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 角色
CREATE TABLE IF NOT EXISTS auth_role (
                                         id           BIGINT UNSIGNED PRIMARY KEY,
                                         tenant_id    BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                         code         VARCHAR(64)  NOT NULL,
                                         name         VARCHAR(128) NOT NULL,
                                         level        SMALLINT NOT NULL DEFAULT 100 COMMENT '数值越小级别越高,可用于数据隔离',
                                         built_in     TINYINT(1) NOT NULL DEFAULT 0,
                                         status       TINYINT NOT NULL DEFAULT 1,
                                         remark       VARCHAR(255),
                                         created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                         updated_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                         deleted      TINYINT(1) NOT NULL DEFAULT 0,
                                         version      BIGINT NOT NULL DEFAULT 0,
                                         UNIQUE KEY uk_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 权限（资源/菜单/按钮/数据权限 等）
CREATE TABLE IF NOT EXISTS auth_permission (
                                               id              BIGINT UNSIGNED PRIMARY KEY,
                                               tenant_id       BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                               type            VARCHAR(16) NOT NULL COMMENT 'api/menu/button/data',
                                               code            VARCHAR(128) NOT NULL,
                                               name            VARCHAR(128) NOT NULL,
                                               resource        VARCHAR(512)     NULL COMMENT 'API路径/资源表达式(如 ANT/URI/自定义code)',
                                               http_method     VARCHAR(16)      NULL COMMENT 'GET/POST/... 仅type=api使用',
                                               action          VARCHAR(64)      NULL COMMENT '业务动作名, 如 video:upload',
                                               parent_id       BIGINT UNSIGNED  NULL COMMENT '菜单/按钮树',
                                               order_no        INT NOT NULL DEFAULT 0,
                                               status          TINYINT NOT NULL DEFAULT 1,
                                               created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                               updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                               deleted         TINYINT(1) NOT NULL DEFAULT 0,
                                               version         BIGINT NOT NULL DEFAULT 0,
                                               UNIQUE KEY uk_tenant_perm_code (tenant_id, code),
                                               KEY idx_parent (parent_id),
                                               KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 用户-角色
CREATE TABLE IF NOT EXISTS auth_user_role (
                                              user_id     BIGINT UNSIGNED NOT NULL,
                                              role_id     BIGINT UNSIGNED NOT NULL,
                                              tenant_id   BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                              created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                              PRIMARY KEY (user_id, role_id, tenant_id),
                                              KEY idx_role (role_id),
                                              CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user_user(id)
                                                  ON DELETE CASCADE ON UPDATE RESTRICT,
                                              CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES auth_role(id)
                                                  ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 角色-权限
CREATE TABLE IF NOT EXISTS auth_role_perm (
                                              role_id     BIGINT UNSIGNED NOT NULL,
                                              perm_id     BIGINT UNSIGNED NOT NULL,
                                              tenant_id   BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                              created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                              PRIMARY KEY (role_id, perm_id, tenant_id),
                                              KEY idx_perm (perm_id),
                                              CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES auth_role(id)
                                                  ON DELETE CASCADE ON UPDATE RESTRICT,
                                              CONSTRAINT fk_rp_perm FOREIGN KEY (perm_id) REFERENCES auth_permission(id)
                                                  ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Refresh Token（只存哈希，轮换/吊销）
CREATE TABLE IF NOT EXISTS auth_refresh_token (
                                                  id             BIGINT UNSIGNED PRIMARY KEY,
                                                  tenant_id      BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                                  user_id        BIGINT UNSIGNED NOT NULL,
                                                  client_id      VARCHAR(64) NOT NULL DEFAULT 'web',
                                                  token_hash     CHAR(64) NOT NULL COMMENT 'SHA-256(refreshToken)',
                                                  issued_at      DATETIME(3) NOT NULL,
                                                  expires_at     DATETIME(3) NOT NULL,
                                                  revoked_at     DATETIME(3),
                                                  revoked_reason VARCHAR(128),
                                                  ip_address     VARCHAR(45),
                                                  user_agent     VARCHAR(255),
                                                  created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                                  UNIQUE KEY uk_token_hash (token_hash),
                                                  KEY idx_user (user_id, expires_at),
                                                  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES user_user(id)
                                                      ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 访问令牌黑名单（存 access JWT 的 JTI 哈希，支持登出/封禁）
CREATE TABLE IF NOT EXISTS auth_access_denylist (
                                                    id          BIGINT UNSIGNED PRIMARY KEY,
                                                    tenant_id   BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                                    jti_hash    CHAR(64) NOT NULL COMMENT 'SHA-256(JTI)',
                                                    reason      VARCHAR(128),
                                                    expires_at  DATETIME(3) NOT NULL,
                                                    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                                    UNIQUE KEY uk_jti (jti_hash),
                                                    KEY idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- MFA 因子（TOTP/SMS/Email/硬件Key）
CREATE TABLE IF NOT EXISTS auth_mfa_factor (
                                               id            BIGINT UNSIGNED PRIMARY KEY,
                                               user_id       BIGINT UNSIGNED NOT NULL,
                                               type          VARCHAR(16) NOT NULL COMMENT 'TOTP/SMS/EMAIL/WEBAUTHN',
                                               secret_enc    VARBINARY(512) NULL COMMENT '密钥密文(如TOTP secret，KMS/SM4等加密)',
                                               phone         VARCHAR(32),
                                               email         VARCHAR(255),
                                               enabled       TINYINT(1) NOT NULL DEFAULT 0,
                                               created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                               updated_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                               UNIQUE KEY uk_user_type (user_id, type),
                                               CONSTRAINT fk_mfa_user FOREIGN KEY (user_id) REFERENCES user_user(id)
                                                   ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- MFA 备用码（一次性，存哈希）
CREATE TABLE IF NOT EXISTS auth_mfa_backup_code (
                                                    id          BIGINT UNSIGNED PRIMARY KEY,
                                                    user_id     BIGINT UNSIGNED NOT NULL,
                                                    code_hash   CHAR(64) NOT NULL,
                                                    used_at     DATETIME(3),
                                                    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                                    UNIQUE KEY uk_user_code (user_id, code_hash),
                                                    CONSTRAINT fk_mfa_code_user FOREIGN KEY (user_id) REFERENCES user_user(id)
                                                        ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 登录日志
CREATE TABLE IF NOT EXISTS auth_login_log (
                                              id            BIGINT UNSIGNED PRIMARY KEY,
                                              tenant_id     BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                              user_id       BIGINT UNSIGNED,
                                              account_id    BIGINT UNSIGNED,
                                              success       TINYINT(1) NOT NULL,
                                              reason        VARCHAR(64),
                                              ip_address    VARCHAR(45),
                                              user_agent    VARCHAR(255),
                                              login_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                              geo           VARCHAR(128),
                                              KEY idx_user_time (user_id, login_at),
                                              KEY idx_tenant_time (tenant_id, login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 密码重置/验证令牌（仅存哈希）
CREATE TABLE IF NOT EXISTS auth_password_reset (
                                                   id             BIGINT UNSIGNED PRIMARY KEY,
                                                   tenant_id      BIGINT UNSIGNED NOT NULL DEFAULT 0,
                                                   account_id     BIGINT UNSIGNED NOT NULL,
                                                   token_hash     CHAR(64) NOT NULL,
                                                   expires_at     DATETIME(3) NOT NULL,
                                                   used_at        DATETIME(3),
                                                   request_ip     VARCHAR(45),
                                                   created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                                   UNIQUE KEY uk_token (token_hash),
                                                   KEY idx_account (account_id),
                                                   CONSTRAINT fk_pr_account FOREIGN KEY (account_id) REFERENCES auth_account(id)
                                                       ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 角色
INSERT INTO auth_role (id, tenant_id, code, name, level, built_in, status, created_at, updated_at, deleted, version)
VALUES (1001, 0, 'ADMIN', '系统管理员', 10, 1, 1, NOW(3), NOW(3), 0, 0),
       (1002, 0, 'USER',  '普通用户',   100, 1, 1, NOW(3), NOW(3), 0, 0);

-- 权限（示例API资源）
INSERT INTO auth_permission (id, tenant_id, type, code, name, resource, http_method, status, created_at, updated_at, deleted, version)
VALUES (2001, 0, 'api', 'user:read',  '用户查询', '/api/users/**',  'GET', 1, NOW(3), NOW(3), 0, 0),
       (2002, 0, 'api', 'user:write', '用户维护', '/api/users/**',  'POST',1, NOW(3), NOW(3), 0, 0);

-- 角色-权限
INSERT INTO auth_role_perm (role_id, perm_id, tenant_id, created_at)
VALUES (1001, 2001, 0, NOW(3)), (1001, 2002, 0, NOW(3)), (1002, 2001, 0, NOW(3));