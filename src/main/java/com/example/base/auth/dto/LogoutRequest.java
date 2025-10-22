package com.example.base.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogoutRequest {
    @Builder.Default
    boolean allSessions = false;
}
