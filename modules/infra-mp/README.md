# infra-mp

基于 MyBatis-Plus 的基础设施模块：
- `MyBatisPlusConfig`：分页、乐观锁、逻辑删除、驼峰映射、枚举处理器，审计字段自动填充
- 多租户（可选）：`TenantLineInnerInterceptor` + `TenantResolver` + `TenantProperties`
- `BaseEntity`：通用实体基类（id/createdAt/updatedAt/deleted/version）
- `MpPageUtils`：IPage ↔ PageResult 转换

## 配置示例（web-api/application.yml）
```yaml
app:
  tenant:
    enabled: true
    column: tenant_id
    default-tenant-id: 0
    ignore-tables: ["sys_tenant"]
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: ASSIGN_ID
```

## 说明
- 若启用多租户，`TenantResolver` 默认从 JWT 的 `tenant` claim 或请求头 `X-Tenant-Id` 读取；解析不到时落到 `default-tenant-id`。
- 审计字段由 `AuditMetaObjectHandler` 自动填充，字段名要求为 `createdAt` 与 `updatedAt`。