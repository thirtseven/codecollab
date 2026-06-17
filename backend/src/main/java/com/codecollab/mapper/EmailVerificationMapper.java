package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.entity.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface EmailVerificationMapper extends BaseMapper<EmailVerification> {
    @Select("SELECT * FROM email_verifications WHERE email = #{email} AND type = #{type} AND used = 0 AND expire_time > #{now} ORDER BY created_at DESC LIMIT 1")
    EmailVerification findValidCode(@Param("email") String email, @Param("type") String type, @Param("now") LocalDateTime now);
}
