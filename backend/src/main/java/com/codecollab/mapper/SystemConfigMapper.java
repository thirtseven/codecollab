package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    @Select("SELECT config_value FROM system_configs WHERE config_key = #{key}")
    String selectValueByKey(@Param("key") String key);
}
