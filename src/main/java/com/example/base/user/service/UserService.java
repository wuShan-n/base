package com.example.base.user.service;

import com.example.base.auth.dto.AssignRolesRequest;
import com.example.base.auth.dto.RoleDto;
import com.example.base.common.api.PageResponse;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;
import com.example.base.user.dto.UserPageQuery;
import com.example.base.user.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserDto createUser(UserCreateRequest request);

    UserDto getUser(Long id);

    void updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    PageResponse<UserDto> pageUsers(UserPageQuery query);

    List<UserDto> listUsersByIds(List<Long> ids);

    List<RoleDto> listUserRoles(Long userId);

    void replaceUserRoles(Long userId, AssignRolesRequest request);
}
