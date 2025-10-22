package com.example.base.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.base.security.tenant.TenantContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisAuditingMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "tenantId", Long.class, TenantContext.getTenantId());
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
        strictInsertFill(metaObject, "version", Long.class, 0L);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
