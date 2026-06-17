package com.codecollab.exception;

import com.codecollab.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("Business exception at {}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception at {}: ", request.getRequestURI(), e);
        return Result.error(500, "Internal server error");
    }
}
