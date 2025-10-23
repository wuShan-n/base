package com.example.base.auth.controller;

import com.example.base.auth.dto.AssignPermissionsRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.RoleCreateRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.dto.RoleUpdateRequest;
import com.example.base.auth.service.RoleService;
import com.example.base.common.api.ApiResponse;
import com.example.base.common.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/roles")
@Validated
@RequiredArgsConstructor
public class RoleController {

    private static final long MAX_PAGE_SIZE = 200L;

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<PageResponse<RoleDto>> listRoles(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        List<RoleDto> roles = roleService.listRoles();
        List<RoleDto> filtered = roles;
        if (keyword != null && !keyword.isBlank()) {
            String lowered = keyword.toLowerCase(Locale.ROOT);
            filtered = roles.stream()
                    .filter(role -> (role.getCode() != null && role.getCode().toLowerCase(Locale.ROOT).contains(lowered))
                            || (role.getName() != null && role.getName().toLowerCase(Locale.ROOT).contains(lowered)))
                    .toList();
        }
        long normalizedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        long normalizedPageNo = Math.max(pageNo, 1);
        long total = filtered.size();
        int fromIndex = (int) Math.min((normalizedPageNo - 1) * normalizedPageSize, total);
        int toIndex = (int) Math.min(fromIndex + normalizedPageSize, total);
        List<RoleDto> items = filtered.subList(fromIndex, toIndex);
        return ApiResponse.success(PageResponse.of(normalizedPageNo, normalizedPageSize, total, items));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleDto role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(role));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDto> getRole(@PathVariable("id") Long id) {
        return ApiResponse.success(roleService.getRole(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateRole(@PathVariable("id") Long id,
                                           @Valid @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<PermissionDto>> listRolePermissions(@PathVariable("id") Long id) {
        RoleDto role = roleService.getRole(id);
        return ApiResponse.success(role.getPermissions());
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> replaceRolePermissions(@PathVariable("id") Long id,
                                                       @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.replaceRolePermissions(id, request);
        return ResponseEntity.noContent().build();
    }
}
