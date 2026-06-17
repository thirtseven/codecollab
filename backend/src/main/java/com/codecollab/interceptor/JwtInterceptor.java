package com.codecollab.interceptor;

import com.codecollab.common.Constants;
import com.codecollab.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.codecollab.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token == null || !token.startsWith(Constants.TOKEN_PREFIX)) {
            sendError(response, 401, "Please login first");
            return false;
        }

        token = token.substring(Constants.TOKEN_PREFIX.length());
        if (!jwtUtil.validateToken(token)) {
            sendError(response, 401, "Token expired or invalid");
            return false;
        }

        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("role", jwtUtil.getRoleFromToken(token));
        return true;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
