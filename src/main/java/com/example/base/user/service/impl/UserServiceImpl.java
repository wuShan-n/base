package com.example.base.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.auth.dto.AccountDto;
import com.example.base.auth.dto.AssignRolesRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.entity.AuthPermissionEntity;
import com.example.base.auth.entity.AuthRoleEntity;
import com.example.base.auth.entity.AuthRolePermEntity;
import com.example.base.auth.entity.AuthUserRoleEntity;
import com.example.base.auth.mapper.AuthPermissionMapper;
import com.example.base.auth.mapper.AuthRoleMapper;
import com.example.base.auth.mapper.AuthRolePermMapper;
import com.example.base.auth.mapper.AuthUserRoleMapper;
import com.example.base.auth.service.AccountService;
import com.example.base.common.api.PageResponse;
import com.example.base.common.enums.Gender;
import com.example.base.common.enums.UserStatus;
import com.example.base.common.exception.BizException;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import com.example.base.user.dto.UserPageQuery;
import com.example.base.user.dto.UserUpdateRequest;
import com.example.base.user.entity.UserEntity;
import com.example.base.user.mapper.UserMapper;
import com.example.base.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private final AccountService accountService;
    private final AuthUserRoleMapper userRoleMapper;
    private final AuthRoleMapper roleMapper;
    private final AuthRolePermMapper rolePermMapper;
    private final AuthPermissionMapper permissionMapper;

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        UserEntity entity = UserEntity.builder()
                .nickname(request.getNickname())
                .avatarUrl(request.getAvatarUrl())
                .gender(Gender.fromCode(request.getGender()))
                .timezone(request.getTimezone())
                .locale(Optional.ofNullable(request.getLocale()).orElse("zh_CN"))
                .status(UserStatus.ACTIVE)
                .tokenVersion(0)
                .remark(request.getRemark())
                .build();
        save(entity);
        request.getAccounts().forEach(account -> accountService.createAccount(entity.getId(), account));
        return getUser(entity.getId());
    }

    @Override
    public UserDto getUser(Long id) {
        UserEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
        List<AccountDto> accounts = accountService.listAccountsByUserId(id);
        List<RoleDto> roles = loadUserRoles(id);
        return toDto(entity, accounts, roles);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        UserEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
        UserEntity updated = entity.toBuilder()
                .nickname(Optional.ofNullable(request.getNickname()).orElse(entity.getNickname()))
                .gender(request.getGender() == null ? entity.getGender() : Gender.fromCode(request.getGender()))
                .avatarUrl(Optional.ofNullable(request.getAvatarUrl()).orElse(entity.getAvatarUrl()))
                .locale(Optional.ofNullable(request.getLocale()).orElse(entity.getLocale()))
                .timezone(Optional.ofNullable(request.getTimezone()).orElse(entity.getTimezone()))
                .status(request.getStatus() == null ? entity.getStatus() : UserStatus.fromCode(request.getStatus()))
                .remark(Optional.ofNullable(request.getRemark()).orElse(entity.getRemark()))
                .build();
        updateById(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        boolean removed = removeById(id);
        if (!removed) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
    }

    @Override
    public PageResponse<UserDto> pageUsers(UserPageQuery query) {
        long pageNo = Math.max(query.getPageNo(), 1);
        long pageSize = Math.min(Math.max(query.getPageSize(), 1), 200);
        Page<UserEntity> page = lambdaQuery()
                .like(query.getKeyword() != null, UserEntity::getNickname, query.getKeyword())
                .eq(query.getStatus() != null, UserEntity::getStatus, UserStatus.fromCode(query.getStatus()))
                .orderByDesc(UserEntity::getCreatedAt)
                .page(Page.of(pageNo, pageSize));

        if (page.getRecords().isEmpty()) {
            return PageResponse.empty(pageNo, pageSize);
        }
        List<Long> userIds = page.getRecords().stream().map(UserEntity::getId).toList();
        Map<Long, List<AccountDto>> accountMap = userIds.stream()
                .collect(Collectors.toMap(Function.identity(), accountService::listAccountsByUserId));
        Map<Long, List<RoleDto>> roleMap = loadRolesForUsers(userIds);
        List<UserDto> items = page.getRecords().stream()
                .map(entity -> toDto(entity,
                        accountMap.getOrDefault(entity.getId(), Collections.emptyList()),
                        roleMap.getOrDefault(entity.getId(), Collections.emptyList())))
                .toList();
        return PageResponse.of(page.getCurrent(), page.getSize(), page.getTotal(), items);
    }

    @Override
    public List<UserDto> listUsersByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<UserEntity> users = listByIds(ids);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<AccountDto>> accountMap = ids.stream()
                .collect(Collectors.toMap(Function.identity(), accountService::listAccountsByUserId));
        Map<Long, List<RoleDto>> roleMap = loadRolesForUsers(ids);
        return users.stream()
                .map(entity -> toDto(entity,
                        accountMap.getOrDefault(entity.getId(), Collections.emptyList()),
                        roleMap.getOrDefault(entity.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    public List<RoleDto> listUserRoles(Long userId) {
        return loadUserRoles(userId);
    }

    @Override
    @Transactional
    public void replaceUserRoles(Long userId, AssignRolesRequest request) {
        UserEntity user = getById(userId);
        if (user == null) {
            throw new BizException("USER_NOT_FOUND", "User not found");
        }
        List<Long> roleIds = request != null ? request.getRoleIds() : Collections.emptyList();
        if (CollUtil.isNotEmpty(roleIds)) {
            List<AuthRoleEntity> roles = roleMapper.selectBatchIds(roleIds);
            long distinctCount = roleIds.stream().distinct().count();
            if (roles.size() != distinctCount) {
                throw new BizException("ROLE_NOT_FOUND", "Some roles do not exist");
            }
        }
        userRoleMapper.delete(new LambdaQueryWrapper<AuthUserRoleEntity>()
                .eq(AuthUserRoleEntity::getUserId, userId));
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        roleIds.stream().distinct().forEach(roleId -> {
            AuthUserRoleEntity relation = AuthUserRoleEntity.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .createdAt(now)
                    .build();
            userRoleMapper.insert(relation);
        });
    }

    private List<RoleDto> loadUserRoles(Long userId) {
        Map<Long, List<RoleDto>> roleMap = loadRolesForUsers(Collections.singletonList(userId));
        return roleMap.getOrDefault(userId, Collections.emptyList());
    }

    private Map<Long, List<RoleDto>> loadRolesForUsers(List<Long> userIds) {
        List<AuthUserRoleEntity> relations = userRoleMapper.selectList(new LambdaQueryWrapper<AuthUserRoleEntity>()
                .in(AuthUserRoleEntity::getUserId, userIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<Long>> userRoleIds = relations.stream()
                .collect(Collectors.groupingBy(AuthUserRoleEntity::getUserId,
                        Collectors.mapping(AuthUserRoleEntity::getRoleId, Collectors.toList())));
        Set<Long> roleIds = relations.stream().map(AuthUserRoleEntity::getRoleId).collect(Collectors.toSet());
        List<AuthRoleEntity> roles = roleMapper.selectBatchIds(roleIds);
        Map<Long, AuthRoleEntity> roleMap = roles.stream()
                .collect(Collectors.toMap(AuthRoleEntity::getId, Function.identity()));

        Map<Long, List<AuthRolePermEntity>> rolePermGroup = rolePermMapper.selectList(new LambdaQueryWrapper<AuthRolePermEntity>()
                        .in(AuthRolePermEntity::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(AuthRolePermEntity::getRoleId));
        Set<Long> permIds = rolePermGroup.values().stream()
                .flatMap(List::stream)
                .map(AuthRolePermEntity::getPermId)
                .collect(Collectors.toSet());
        Map<Long, PermissionDto> permMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(permIds)) {
            List<AuthPermissionEntity> perms = permissionMapper.selectBatchIds(permIds);
            permMap = perms.stream().collect(Collectors.toMap(AuthPermissionEntity::getId, this::toPermissionDto));
        }
        Map<Long, PermissionDto> finalPermMap = permMap;
        Map<Long, List<PermissionDto>> rolePermDtos = rolePermGroup.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(rel -> finalPermMap.get(rel.getPermId()))
                                .filter(Objects::nonNull)
                                .toList()));

        Map<Long, List<RoleDto>> result = userRoleIds.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(roleMap::get)
                                .filter(Objects::nonNull)
                                .map(role -> toRoleDto(role, rolePermDtos.getOrDefault(role.getId(), Collections.emptyList())))
                                .toList()));
        return result;
    }

    private UserDto toDto(UserEntity entity, List<AccountDto> accounts, List<RoleDto> roles) {
        return UserDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .gender(entity.getGender())
                .timezone(entity.getTimezone())
                .locale(entity.getLocale())
                .status(entity.getStatus())
                .tokenVersion(entity.getTokenVersion())
                .lastLoginAt(entity.getLastLoginAt())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .accounts(accounts)
                .roles(roles)
                .build();
    }

    private RoleDto toRoleDto(AuthRoleEntity role, List<PermissionDto> permissions) {
        return RoleDto.builder()
                .id(role.getId())
                .tenantId(role.getTenantId())
                .code(role.getCode())
                .name(role.getName())
                .level(role.getLevel())
                .builtIn(Boolean.TRUE.equals(role.getBuiltIn()))
                .status(role.getStatus())
                .remark(role.getRemark())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
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
}
