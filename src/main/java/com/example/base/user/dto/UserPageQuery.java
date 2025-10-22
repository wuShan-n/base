package com.example.base.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserPageQuery {
    @Builder.Default
    long pageNo = 1;
    @Builder.Default
    long pageSize = 20;
    String keyword;
    Integer status;
}
