package com.example.base.user.controller;

import com.example.base.auth.dto.AccountCreateRequest;
import com.example.base.auth.dto.AccountDto;
import com.example.base.auth.dto.AssignRolesRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.enums.AccountStatus;
import com.example.base.auth.enums.PermissionType;
import com.example.base.auth.service.AccountService;
import com.example.base.common.api.ApiResponse;
import com.example.base.common.api.PageResponse;
import com.example.base.common.enums.Gender;
import com.example.base.common.enums.UserStatus;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import com.example.base.user.dto.UserPageQuery;
import com.example.base.user.dto.UserUpdateRequest;
import com.example.base.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
//@SpringBootTest
class UserControllerTest {

    private UserService userService;

    private AccountService accountService;

    private UserController userController;

    @Test
    void pageUsersShouldReturnPageData() {
        PageResponse<UserDto> page = PageResponse.of(1, 20, 1, List.of(sampleUserDto()));
        when(userService.pageUsers(any(UserPageQuery.class))).thenReturn(page);

        ApiResponse<PageResponse<UserDto>> response = userController.pageUsers(1, 20, "alice", 1);
        assertThat(response.getData().getTotal()).isEqualTo(1);
        assertThat(response.getData().getItems().getFirst().getNickname()).isEqualTo("Alice");

        ArgumentCaptor<UserPageQuery> captor = ArgumentCaptor.forClass(UserPageQuery.class);
        verify(userService).pageUsers(captor.capture());
        assertThat(captor.getValue().getKeyword()).isEqualTo("alice");
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    void createUserShouldReturnCreatedUser() {
        UserDto userDto = sampleUserDto();
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userDto);

        UserCreateRequest request = UserCreateRequest.builder()
                .nickname("Alice")
                .account(AccountCreateRequest.builder()
                        .identityType("username")
                        .identifier("alice")
                        .credential("secret123")
                        .build())
                .build();

        ResponseEntity<ApiResponse<UserDto>> response = userController.createUser(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getNickname()).isEqualTo("Alice");
        verify(userService).createUser(request);
    }

    @Test
    void getUserShouldReturnDetails() {
        when(userService.getUser(1L)).thenReturn(sampleUserDto());

        ApiResponse<UserDto> response = userController.getUser(1L);
        assertThat(response.getData().getId()).isEqualTo(1L);
        verify(userService).getUser(1L);
    }

    @Test
    void updateUserShouldInvokeService() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname("Alice Updated")
                .build();

        ResponseEntity<Void> response = userController.updateUser(1L, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).updateUser(1L, request);
    }

    @Test
    void deleteUserShouldReturnNoContent() {
        ResponseEntity<Void> response = userController.deleteUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(2L);
    }

    @Test
    void listUserRolesShouldReturnRoles() {
        when(userService.listUserRoles(1L)).thenReturn(List.of(sampleRoleDto()));

        ApiResponse<List<RoleDto>> response = userController.listUserRoles(1L);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().getFirst().getCode()).isEqualTo("ADMIN");
        verify(userService).listUserRoles(1L);
    }

    @Test
    void replaceUserRolesShouldForwardRequest() {
        AssignRolesRequest request = AssignRolesRequest.builder()
                .roleId(10L)
                .roleId(11L)
                .build();

        ResponseEntity<Void> response = userController.replaceUserRoles(3L, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).replaceUserRoles(3L, request);
    }

    @Test
    void createAccountShouldReturnCreatedAccount() {
        AccountDto accountDto = sampleAccountDto();
        when(accountService.createAccount(eq(1L), any(AccountCreateRequest.class))).thenReturn(accountDto);

        AccountCreateRequest request = AccountCreateRequest.builder()
                .identityType("username")
                .identifier("alice")
                .credential("secret123")
                .primary(true)
                .build();

        ResponseEntity<ApiResponse<AccountDto>> response = userController.createAccount(1L, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getIdentifier()).isEqualTo("alice");
        verify(accountService).createAccount(1L, request);
    }

    @Test
    void listAccountsShouldReturnAccountList() {
        when(accountService.listAccountsByUserId(1L)).thenReturn(List.of(sampleAccountDto()));

        ApiResponse<List<AccountDto>> response = userController.listUserAccounts(1L);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().getFirst().getIdentifier()).isEqualTo("alice");
        verify(accountService).listAccountsByUserId(1L);
    }

    @Test
    void deleteAccountShouldReturnNoContent() {
        ResponseEntity<Void> response = userController.deleteAccount(1L, 200L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(accountService).deleteAccount(1L, 200L);
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
                .accounts(Collections.emptyList())
                .roles(Collections.emptyList())
                .build();
    }

    private RoleDto sampleRoleDto() {
        return RoleDto.builder()
                .id(10L)
                .tenantId(0L)
                .code("ADMIN")
                .name("Administrator")
                .level(1)
                .builtIn(false)
                .status(1)
                .remark(null)
                .permissions(List.of(
                        com.example.base.auth.dto.PermissionDto.builder()
                                .id(100L)
                                .tenantId(0L)
                                .type(PermissionType.API)
                                .code("user:read")
                                .name("Read Users")
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AccountDto sampleAccountDto() {
        return AccountDto.builder()
                .id(100L)
                .tenantId(0L)
                .userId(1L)
                .identityType("username")
                .identifier("alice")
                .passwordVersion(0)
                .primary(true)
                .verified(false)
                .verifiedAt(null)
                .failedAttempts(0)
                .lockUntil(null)
                .lastLoginAt(null)
                .meta(Map.of())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
