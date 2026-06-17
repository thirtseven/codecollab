package com.codecollab.common;

public class Constants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final long VERIFICATION_CODE_EXPIRE = 300000L;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_LOCK_TIME = 1800000L;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_BANNED = -1;

    public static final String EMAIL_TYPE_REGISTER = "REGISTER";
    public static final String EMAIL_TYPE_RESET = "RESET_PASSWORD";
    public static final String EMAIL_TYPE_INVITE = "TEAM_INVITE";

    public static final String[] PUBLIC_URLS = {
        "/auth/login", "/auth/register", "/auth/send-code",
        "/auth/verify-email", "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**"
    };
}
