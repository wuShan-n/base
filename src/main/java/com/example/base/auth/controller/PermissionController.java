package com.example.base.auth.controller;

import com.example.base.auth.dto.PermissionCreateRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.PermissionUpdateRequest;
import com.example.base.auth.service.PermissionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/permissions")
@Validated
@RequiredArgsConstructor
public class PermissionController {

    private static final long MAX_PAGE_SIZE = 200L;

    private final PermissionService permissionService;

    @GetMapping
    public ApiResponse<PageResponse<PermissionDto>> listPermissions(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword
    ) {
        List<PermissionDto> permissions = permissionService.listPermissions();
        List<PermissionDto> filtered = permissions.stream()
                .filter(permission -> type == null
                        || (permission.getType() != null
                        && permission.getType().getCode().equalsIgnoreCase(type)))
                .filter(permission -> {
                    if (keyword == null || keyword.isBlank()) {
                        return true;
                    }
                    String lowered = keyword.toLowerCase(Locale.ROOT);
                    return (permission.getCode() != null && permission.getCode().toLowerCase(Locale.ROOT).contains(lowered))
                            || (permission.getName() != null && permission.getName().toLowerCase(Locale.ROOT).contains(lowered));
                })
                .toList();
        long normalizedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        long normalizedPageNo = Math.max(pageNo, 1);
        long total = filtered.size();
        int fromIndex = (int) Math.min((normalizedPageNo - 1) * normalizedPageSize, total);
        int toIndex = (int) Math.min(fromIndex + normalizedPageSize, total);
        List<PermissionDto> items = filtered.subList(fromIndex, toIndex);
        return ApiResponse.success(PageResponse.of(normalizedPageNo, normalizedPageSize, total, items));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionDto>> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionDto permission = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(permission));
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionDto> getPermission(@PathVariable("id") Long id) {
        return ApiResponse.success(permissionService.getPermission(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePermission(@PathVariable("id") Long id,
                                                 @Valid @RequestBody PermissionUpdateRequest request) {
        permissionService.updatePermission(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
