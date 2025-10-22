package com.example.base.auth.service;

import com.example.base.auth.dto.LoginRequest;
import com.example.base.auth.dto.LogoutRequest;
import com.example.base.auth.dto.RefreshRequest;
import com.example.base.auth.dto.TokenResponse;
import com.example.base.user.dto.UserCreateRequest;
import com.example.base.user.dto.UserDto;

public interface AuthService {

    UserDto register(UserCreateRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    UserDto currentUser();
}
