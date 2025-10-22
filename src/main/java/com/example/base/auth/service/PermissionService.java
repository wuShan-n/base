package com.example.base.auth.service;

import com.example.base.auth.dto.PermissionCreateRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.PermissionUpdateRequest;

import java.util.List;

public interface PermissionService {
    PermissionDto createPermission(PermissionCreateRequest request);

    void updatePermission(Long id, PermissionUpdateRequest request);

    PermissionDto getPermission(Long id);

    void deletePermission(Long id);

    List<PermissionDto> listPermissions();
}
