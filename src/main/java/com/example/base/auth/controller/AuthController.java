package com.example.base.auth.controller;

import com.example.base.auth.dto.CurrentUserResponse;
import com.example.base.auth.dto.LoginRequest;
import com.example.base.auth.dto.LogoutRequest;
import com.example.base.auth.dto.PermissionDto;
import com.example.base.auth.dto.RefreshRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.dto.TokenResponse;
import com.example.base.auth.service.AuthService;
import com.example.base.common.api.ApiResponse;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody UserCreateRequest request) {
        UserDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> currentUser() {
        UserDto user = authService.currentUser();
        List<RoleDto> roles = Optional.ofNullable(user.getRoles()).orElse(Collections.emptyList());
        List<String> permissions = roles.stream()
                .flatMap(role -> Optional.ofNullable(role.getPermissions()).orElse(Collections.emptyList()).stream())
                .map(PermissionDto::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        CurrentUserResponse response = CurrentUserResponse.builder()
                .user(user)
                .roles(roles)
                .permissions(permissions)
                .build();
        return ApiResponse.success(response);
    }
}
