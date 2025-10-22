package com.example.base.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.auth.dto.*;
import com.example.base.auth.entity.AuthPermissionEntity;
import com.example.base.auth.entity.AuthRoleEntity;
import com.example.base.auth.entity.AuthRolePermEntity;
import com.example.base.auth.mapper.AuthPermissionMapper;
import com.example.base.auth.mapper.AuthRoleMapper;
import com.example.base.auth.mapper.AuthRolePermMapper;
import com.example.base.auth.service.RoleService;
import com.example.base.common.exception.BizException;
import com.example.base.security.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<AuthRoleMapper, AuthRoleEntity> implements RoleService {

    private final AuthRolePermMapper rolePermMapper;
    private final AuthPermissionMapper permissionMapper;

    @Override
    @Transactional
    public RoleDto createRole(RoleCreateRequest request) {
        validateRoleCodeUnique(request.getCode());
        AuthRoleEntity entity = AuthRoleEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .level(Optional.ofNullable(request.getLevel()).orElse(100))
                .builtIn(false)
                .status(Optional.ofNullable(request.getStatus()).orElse(1))
                .remark(request.getRemark())
                .build();
        save(entity);
        return toDto(entity, Collections.emptyList());
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleUpdateRequest request) {
        AuthRoleEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("ROLE_NOT_FOUND", "Role not found");
        }
        AuthRoleEntity updated = entity.toBuilder()
                .name(Optional.ofNullable(request.getName()).orElse(entity.getName()))
                .level(Optional.ofNullable(request.getLevel()).orElse(entity.getLevel()))
                .status(Optional.ofNullable(request.getStatus()).orElse(entity.getStatus()))
                .remark(Optional.ofNullable(request.getRemark()).orElse(entity.getRemark()))
                .build();
        updateById(updated);
    }

    @Override
    public RoleDto getRole(Long id) {
        AuthRoleEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("ROLE_NOT_FOUND", "Role not found");
        }
        List<PermissionDto> permissions = listPermissionsByRoleIds(Collections.singleton(id)).getOrDefault(id, Collections.emptyList());
        return toDto(entity, permissions);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        AuthRoleEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("ROLE_NOT_FOUND", "Role not found");
        }
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw new BizException("ROLE_BUILT_IN", "Built-in role cannot be deleted");
        }
        removeById(id);
    }

    @Override
    public List<RoleDto> listRoles() {
        List<AuthRoleEntity> roles = list();
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> roleIds = roles.stream().map(AuthRoleEntity::getId).collect(Collectors.toSet());
        Map<Long, List<PermissionDto>> permissionMap = listPermissionsByRoleIds(roleIds);
        return roles.stream()
                .map(role -> toDto(role, permissionMap.getOrDefault(role.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    @Transactional
    public void replaceRolePermissions(Long roleId, AssignPermissionsRequest request) {
        AuthRoleEntity role = getById(roleId);
        if (role == null) {
            throw new BizException("ROLE_NOT_FOUND", "Role not found");
        }
        rolePermMapper.delete(new LambdaQueryWrapper<AuthRolePermEntity>()
                .eq(AuthRolePermEntity::getRoleId, roleId));
        if (CollUtil.isEmpty(request.getPermIds())) {
            return;
        }
        request.getPermIds().stream().distinct().forEach(permId -> {
            AuthRolePermEntity relation = AuthRolePermEntity.builder()
                    .roleId(roleId)
                    .permId(permId)
                    .build();
            rolePermMapper.insert(relation);
        });
    }

    private Map<Long, List<PermissionDto>> listPermissionsByRoleIds(Set<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        List<AuthRolePermEntity> relations = rolePermMapper.selectList(new LambdaQueryWrapper<AuthRolePermEntity>()
                .in(AuthRolePermEntity::getRoleId, roleIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> permIds = relations.stream().map(AuthRolePermEntity::getPermId).collect(Collectors.toSet());
        List<AuthPermissionEntity> permissions = permissionMapper.selectBatchIds(permIds);
        Map<Long, PermissionDto> permMap = permissions.stream()
                .collect(Collectors.toMap(AuthPermissionEntity::getId, this::toPermissionDto));
        return relations.stream()
                .collect(Collectors.groupingBy(AuthRolePermEntity::getRoleId,
                        Collectors.mapping(rel -> permMap.get(rel.getPermId()),
                                Collectors.collectingAndThen(Collectors.toList(),
                                        list -> list.stream().filter(permission -> permission != null).toList()))));
    }

    private RoleDto toDto(AuthRoleEntity entity, List<PermissionDto> permissions) {
        return RoleDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .code(entity.getCode())
                .name(entity.getName())
                .level(entity.getLevel())
                .builtIn(Boolean.TRUE.equals(entity.getBuiltIn()))
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .permissions(permissions)
                .build();
    }

    private PermissionDto toPermissionDto(AuthPermissionEntity entity) {
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

    private void validateRoleCodeUnique(String code) {
        Long tenantId = TenantContext.getTenantId();
        boolean exists = lambdaQuery()
                .eq(AuthRoleEntity::getTenantId, tenantId)
                .eq(AuthRoleEntity::getCode, code)
                .exists();
        if (exists) {
            throw new BizException("ROLE_CODE_EXISTS", "Role code already exists");
        }
    }
}
