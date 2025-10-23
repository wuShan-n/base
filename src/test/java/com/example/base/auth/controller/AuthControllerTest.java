package com.example.base.auth.controller;

import com.example.base.auth.dto.*;
import com.example.base.auth.enums.PermissionType;
import com.example.base.auth.service.AuthService;
import com.example.base.common.api.ApiResponse;
import com.example.base.common.enums.Gender;
import com.example.base.common.enums.UserStatus;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthControllerTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private AuthController authController;

    @Test
    void registerShouldReturnCreatedUser() {

        UserCreateRequest request = UserCreateRequest.builder()
                .nickname("Alice")
                .account(com.example.base.auth.dto.AccountCreateRequest.builder()
                        .identityType("username")
                        .identifier("alice")
                        .credential("secret123")
                        .build())
                .build();

        ResponseEntity<ApiResponse<UserDto>> response = authController.register(request);
        System.out.println(response);
    }

    @Test
    void loginShouldReturnTokenResponse() {


        LoginRequest request = LoginRequest.builder()
                .identityType("username")
                .identifier("alice")
                .credential("secret123")
                .build();

        ApiResponse<TokenResponse> response = authController.login(request);
        System.out.println(response.getData());
    }

    @Test
    void refreshShouldReturnNewTokenResponse() {
        TokenResponse tokenResponse = TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken("access-new")
                .refreshToken("refresh-new")
                .expiresIn(900)
                .jti("jti-2")
                .user(sampleUserDto())
                .build();
        when(authService.refresh(any(RefreshRequest.class))).thenReturn(tokenResponse);

        RefreshRequest request = RefreshRequest.builder()
                .refreshToken("refresh-old")
                .clientId("web")
                .build();

        ApiResponse<TokenResponse> response = authController.refresh(request);
        assertThat(response.getData().getAccessToken()).isEqualTo("access-new");
        verify(authService).refresh(request);
    }

    @Test
    void logoutShouldReturnNoContent() {
        LogoutRequest request = LogoutRequest.builder()
                .allSessions(true)
                .build();

        ResponseEntity<Void> response = authController.logout(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout(request);
    }

    @Test
    void currentUserShouldAggregatePermissions() {
        UserDto userWithRoles = sampleUserDto().toBuilder()
                .roles(List.of(
                        RoleDto.builder()
                                .id(10L)
                                .tenantId(0L)
                                .code("ADMIN")
                                .name("Administrator")
                                .level(1)
                                .builtIn(false)
                                .status(1)
                                .remark(null)
                                .permissions(List.of(
                                        PermissionDto.builder()
                                                .id(100L)
                                                .tenantId(0L)
                                                .type(PermissionType.API)
                                                .code("user:read")
                                                .name("Read Users")
                                                .build(),
                                        PermissionDto.builder()
                                                .id(101L)
                                                .tenantId(0L)
                                                .type(PermissionType.API)
                                                .code("user:write")
                                                .name("Write Users")
                                                .build()
                                ))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build(),
                        RoleDto.builder()
                                .id(11L)
                                .tenantId(0L)
                                .code("OPERATOR")
                                .name("Operator")
                                .level(2)
                                .builtIn(false)
                                .status(1)
                                .remark(null)
                                .permissions(List.of(
                                        PermissionDto.builder()
                                                .id(101L)
                                                .tenantId(0L)
                                                .type(PermissionType.API)
                                                .code("user:write")
                                                .name("Write Users")
                                                .build()
                                ))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ))
                .build();
        when(authService.currentUser()).thenReturn(userWithRoles);

        ApiResponse<com.example.base.auth.dto.CurrentUserResponse> response = authController.currentUser();
        assertThat(response.getData().getPermissions()).containsExactlyInAnyOrder("user:read", "user:write");
        verify(authService).currentUser();
    }

    private UserDto sampleUserDto() {
        return UserDto.builder()
                .id(1L)
                .tenantId(0L)
                .nickname("Alice")
                .avatarUrl(null)
                .gender(Gender.FEMALE)
                .timezone("Asia/Shanghai")
                .locale("zh_CN")
                .status(UserStatus.ACTIVE)
                .tokenVersion(0)
                .lastLoginAt(null)
                .remark(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .accounts(List.of())
                .roles(List.of())
                .build();
    }
}
