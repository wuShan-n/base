package com.example.base.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.auth.entity.AuthPasswordResetEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthPasswordResetMapper extends BaseMapper<AuthPasswordResetEntity> {
}
