package com.example.base.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.auth.dto.PermissionCreateRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.PermissionUpdateRequest;
import com.example.base.auth.entity.AuthPermissionEntity;
import com.example.base.auth.enums.PermissionType;
import com.example.base.auth.mapper.AuthPermissionMapper;
import com.example.base.auth.service.PermissionService;
import com.example.base.common.exception.BizException;
import com.example.base.security.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<AuthPermissionMapper, AuthPermissionEntity> implements PermissionService {

    @Override
    @Transactional
    public PermissionDto createPermission(PermissionCreateRequest request) {
        validatePermissionCodeUnique(request.getCode());
        AuthPermissionEntity entity = AuthPermissionEntity.builder()
                .type(PermissionType.fromCode(request.getType()))
                .code(request.getCode())
                .name(request.getName())
                .resource(request.getResource())
                .httpMethod(request.getHttpMethod())
                .action(request.getAction())
                .parentId(request.getParentId())
                .orderNo(Optional.ofNullable(request.getOrderNo()).orElse(0))
                .status(Optional.ofNullable(request.getStatus()).orElse(1))
                .build();
        save(entity);
        return toDto(entity);
    }

    @Override
    @Transactional
    public void updatePermission(Long id, PermissionUpdateRequest request) {
        AuthPermissionEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("PERMISSION_NOT_FOUND", "Permission not found");
        }
        AuthPermissionEntity updated = entity.toBuilder()
                .name(Optional.ofNullable(request.getName()).orElse(entity.getName()))
                .resource(Optional.ofNullable(request.getResource()).orElse(entity.getResource()))
                .httpMethod(Optional.ofNullable(request.getHttpMethod()).orElse(entity.getHttpMethod()))
                .action(Optional.ofNullable(request.getAction()).orElse(entity.getAction()))
                .parentId(Optional.ofNullable(request.getParentId()).orElse(entity.getParentId()))
                .orderNo(Optional.ofNullable(request.getOrderNo()).orElse(entity.getOrderNo()))
                .status(Optional.ofNullable(request.getStatus()).orElse(entity.getStatus()))
                .build();
        updateById(updated);
    }

    @Override
    public PermissionDto getPermission(Long id) {
        AuthPermissionEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("PERMISSION_NOT_FOUND", "Permission not found");
        }
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        boolean removed = removeById(id);
        if (!removed) {
            throw new BizException("PERMISSION_NOT_FOUND", "Permission not found");
        }
    }

    @Override
    public List<PermissionDto> listPermissions() {
        List<AuthPermissionEntity> permissions = list(new LambdaQueryWrapper<AuthPermissionEntity>()
                .orderByAsc(AuthPermissionEntity::getOrderNo));
        if (permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream().map(this::toDto).toList();
    }

    private PermissionDto toDto(AuthPermissionEntity entity) {
        return PermissionDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .type(entity.getType())
                .code(entity.getCode())
                .name(entity.getName())
                .resource(entity.getResource())
                .httpMethod(entity.getHttpMethod())
                .action(entity.getAction())
                .parentId(entity.getParentId())
                .orderNo(entity.getOrderNo())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private void validatePermissionCodeUnique(String code) {
        Long tenantId = TenantContext.getTenantId();
        boolean exists = lambdaQuery()
                .eq(AuthPermissionEntity::getTenantId, tenantId)
                .eq(AuthPermissionEntity::getCode, code)
                .exists();
        if (exists) {
            throw new BizException("PERMISSION_CODE_EXISTS", "Permission code already exists");
        }
    }
}
