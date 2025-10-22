package com.example.infra.mp.util;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 通用基类实体：
 * - 主键：ASSIGN_ID（雪花）
 * - 审计：createdAt / updatedAt
 * - 逻辑删除：deleted（0/1）
 * - 乐观锁：version
 */
public abstract class BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
