package com.example.base.auth.service;

import com.example.base.auth.dto.AssignPermissionsRequest;
import com.example.base.auth.dto.RoleCreateRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.dto.RoleUpdateRequest;

import java.util.List;

public interface RoleService {
    RoleDto createRole(RoleCreateRequest request);

    void updateRole(Long id, RoleUpdateRequest request);

    RoleDto getRole(Long id);

    void deleteRole(Long id);

    List<RoleDto> listRoles();

    void replaceRolePermissions(Long roleId, AssignPermissionsRequest request);
}
