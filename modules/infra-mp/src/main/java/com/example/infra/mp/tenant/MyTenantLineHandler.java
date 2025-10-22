package com.example.infra.mp.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Locale;
import java.util.Set;

/**
 * MyBatis-Plus 多租户处理器：将 SQL 自动拼接 tenant_id = ?
 */
public class MyTenantLineHandler implements TenantLineHandler {

    private final TenantResolver resolver;
    private final TenantProperties props;
    private final Set<String> ignore;

    public MyTenantLineHandler(TenantResolver resolver, TenantProperties props) {
        this.resolver = resolver;
        this.props = props;
        this.ignore = props.getIgnoreTables();
    }

    @Override
    public Expression getTenantId() {
        long tenantId = resolver.resolveTenantId().orElse(props.getDefaultTenantId());
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return props.getColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) return false;
        return ignore.stream().anyMatch(t -> t.equalsIgnoreCase(tableName));
    }
}
