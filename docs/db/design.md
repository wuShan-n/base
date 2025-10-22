下面给你一套「Spring Boot（JDK 25）+ Spring Security + JWT + MyBatis-Plus + Maven 单体多模块」中**Auth & User**的数据库设计。目标是：

* 支持用户名/邮箱/手机多身份登录、第三方 OAuth 绑定
* 经典 RBAC（用户—角色—权限）
* JWT 无状态鉴权 + Refresh Token 轮换 + 退出/封禁用黑名单
* 选配：MFA（TOTP/SMS/Email）、密码重置、登录日志
* 兼容多租户（可不启用，tenant_id=0 代表默认租户）

## 一、模块划分（简述）

* `common`：基础常量、枚举、通用实体基类（含 MP 审计字段/逻辑删除/乐观锁）
* `user-domain`：用户资料领域（实体、Mapper、Service）
* `auth-domain`：账号、角色、权限、令牌、MFA 等（实体、Mapper、Service）
* `web-api`：Controller、Security 配置（JWT 过滤器、异常处理、方法鉴权）

> 下面主要给**数据库表设计 + MySQL 8 DDL**（utf8mb4）。

---

## 二、ER 关系（文字版）

* 用户（`user_user`）1 — n 账号（`auth_account`）
* 用户 n — n 角色（`auth_user_role`，→ `auth_role`）
* 角色 n — n 权限（`auth_role_perm`，→ `auth_permission`）
* 用户 1 — n RefreshToken（`auth_refresh_token`）
* 黑名单记录（`auth_access_denylist`）按 `jti` 层面失效访问令牌
* 用户 1 — n MFA 因子（`auth_mfa_factor`）、MFA 备用码（`auth_mfa_backup_code`）
* 账号/用户 1 — n 登录日志（`auth_login_log`）
* 账号 1 — n 密码重置/验证令牌（`auth_password_reset`）

---

## 三、建表约定

* 主键：`BIGINT UNSIGNED`（雪花/ULID 可映射），MP `IdType.ASSIGN_ID`
* 审计：`created_at DATETIME(3)`、`updated_at DATETIME(3)`；逻辑删除 `deleted TINYINT(1)`；乐观锁 `version BIGINT`
* 多租户：`tenant_id BIGINT UNSIGNED NOT NULL DEFAULT 0`
* 字符集：`utf8mb4` / `utf8mb4_0900_ai_ci`
* 安全：令牌/验证码仅**存哈希**不存明文；敏感字段加注释

---

## 五、关键设计要点（落地细节）

* **多身份登录**：统一放在 `auth_account(identity_type, identifier, credential_hash)`；保证 `(tenant_id, identity_type, identifier)` 唯一。
* **JWT 建议**

    * access token：短期（如 15 分钟），包含 `sub(userId) / jti / roles / pver(密码版本) / tver(用户全局令牌版本)`。
    * refresh token：长期，服务端只存 `SHA-256(refresh)` 于 `auth_refresh_token`，旋转更新、支持吊销。
    * 登出/封禁：将 access 的 `jti` 哈希写入 `auth_access_denylist` 直到过期；或提升 `token_version` 使全部令牌失效。
* **RBAC**：接口鉴权用 `auth_permission.type='api'` + `resource`（存 Ant/正则/自定义 code），在 Spring Security 中加载用户的 `roles -> perms`。
* **防爆破**：`failed_attempts + lock_until`，在登录失败时自增与时间窗锁定。
* **MFA**：`auth_mfa_factor`（如 TOTP secret 加密存储）、`auth_mfa_backup_code` 存哈希。
* **MyBatis-Plus**

    * 实体建议继承 `BaseEntity`（含 id/created_at/updated_at/deleted/version）。
    * 表字段：`deleted` 对应 `@TableLogic`，`version` 对应 `@Version`。
    * 主键策略：`@TableId(type = IdType.ASSIGN_ID)`。
    * JSON 字段（MySQL）用 `@TableField(typeHandler = JacksonTypeHandler.class)`。
* **索引策略**：登录主查 `uk_tenant_ident`；权限加载批量走 `(role_id)`/`(perm_id)`；日志按 `(user_id, login_at)`、令牌按 `expires_at` 清理。

---


## 七、与 Spring Security/JWT 的字段映射建议

* `UserDetails#username` → 从 `auth_account` 的 `(identity_type='username', identifier)` 取；也可支持邮箱/手机。
* `password` → `credential_hash`（BCrypt/Argon2）。
* `authorities` → JOIN `auth_user_role -> auth_role_perm -> auth_permission`（仅 type=api），在缓存层做 5~10 分钟本地或分布式缓存。
* JWT 载荷携带：

    * `sub` = userId，`tenant` = tenantId，`jti`，`roles`（可选），`pver`=`auth_account.password_version`，`tver`=`user_user.token_version`。
    * 鉴权时若 DB 中 `pver/tver` 发生变化则拒绝。

---

如果你需要，我可以在下一步把 **MyBatis-Plus 实体/Mapper 接口** 和 **Spring Security + JWT 的配置骨架**也一并给到，包括登录/刷新/注销的接口示例与合理的事务/缓存策略。
