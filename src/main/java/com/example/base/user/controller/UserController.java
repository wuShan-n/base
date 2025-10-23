package com.example.base.user.controller;

import com.example.base.auth.dto.AccountCreateRequest;
import com.example.base.auth.dto.AccountDto;
import com.example.base.auth.dto.AssignRolesRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.auth.service.AccountService;
import com.example.base.common.api.ApiResponse;
import com.example.base.common.api.PageResponse;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import com.example.base.user.dto.UserPageQuery;
import com.example.base.user.dto.UserUpdateRequest;
import com.example.base.user.service.UserService;
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

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    @GetMapping
    public ApiResponse<PageResponse<UserDto>> pageUsers(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        UserPageQuery query = UserPageQuery.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .keyword(keyword)
                .status(status)
                .build();
        return ApiResponse.success(userService.pageUsers(query));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable("id") Long id,
                                           @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roles")
    public ApiResponse<List<RoleDto>> listUserRoles(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.listUserRoles(id));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> replaceUserRoles(@PathVariable("id") Long id,
                                                 @Valid @RequestBody AssignRolesRequest request) {
        userService.replaceUserRoles(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/accounts")
    public ApiResponse<List<AccountDto>> listUserAccounts(@PathVariable("id") Long id) {
        return ApiResponse.success(accountService.listAccountsByUserId(id));
    }

    @PostMapping("/{id}/accounts")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(@PathVariable("id") Long id,
                                                                 @Valid @RequestBody AccountCreateRequest request) {
        AccountDto account = accountService.createAccount(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(account));
    }

    @DeleteMapping("/{id}/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id,
                                              @PathVariable("accountId") Long accountId) {
        accountService.deleteAccount(id, accountId);
        return ResponseEntity.noContent().build();
    }
}
