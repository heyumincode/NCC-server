package com.chongchao.mvp.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        LOGGER.info("[biz] code={} msg={}", exception.getCode(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "参数不合法" : error.getDefaultMessage())
                .orElse("参数不合法");
        LOGGER.info("[validate] {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.fail("INVALID_ARGUMENT", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        LOGGER.info("[validate] {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.fail("INVALID_ARGUMENT", exception.getMessage()));
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseError(BadSqlGrammarException exception) {
        String sqlErrorCode = exception.getSQLException() == null
                ? "?"
                : String.valueOf(exception.getSQLException().getErrorCode());
        LOGGER.error("[db] SQL fail code={} sql=\"{}\" msg={}",
                sqlErrorCode,
                truncate(exception.getSql(), 240),
                exception.getMessage(),
                exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("DATABASE_ERROR", "服务暂时不可用，请稍后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        LOGGER.error("[unhandled] {}", exception.toString(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("INTERNAL_ERROR", "服务暂时不可用，请稍后重试"));
    }

    private static String truncate(String s, int max) {
        if (s == null) return "(null)";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}