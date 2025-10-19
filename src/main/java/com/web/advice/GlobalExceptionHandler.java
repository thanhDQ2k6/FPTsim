package com.web.advice;

import com.exception.BusinessException;
import com.exception.NotFoundException;
import com.web.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice // Bắt các lỗi do ứng dụng ném ra (không phải lỗi "đường sai")
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest req) {
        return to(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest req) {
        String code = (ex.getCode() != null) ? ex.getCode() : "BUSINESS";
        return to(HttpStatus.BAD_REQUEST, code, ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        SQLException sql = getSql(ex);
        int vendor = (sql != null) ? sql.getErrorCode() : 0; // MySQL: 1062 dup, 1451/1452 FK
        if (vendor == 1062) return to(HttpStatus.CONFLICT, "DUPLICATE", "Dữ liệu trùng lặp", req);
        if (vendor == 1451) return to(HttpStatus.BAD_REQUEST, "FK_CONFLICT", "Không thể xóa do còn tham chiếu", req);
        if (vendor == 1452) return to(HttpStatus.BAD_REQUEST, "FK_VIOLATION", "Khóa ngoại không hợp lệ", req);
        return to(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY", ex.getMostSpecificCause().getMessage(), req);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> dataAccess(DataAccessException ex, HttpServletRequest req) {
        if (ex instanceof BadSqlGrammarException) {
            return to(HttpStatus.INTERNAL_SERVER_ERROR, "SQL_SYNTAX", ex.getMostSpecificCause().getMessage(), req);
        }
        return to(HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR", ex.getMostSpecificCause().getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> other(Exception ex, HttpServletRequest req) {
        return to(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED", ex.getMessage(), req);
    }

    // Helpers: nên giữ để tránh lặp
    private static ResponseEntity<ApiError> to(HttpStatus status, String code, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(code, msg, req.getRequestURI()));
    }

    private static SQLException getSql(Exception ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException se) return se;
            t = t.getCause();
        }
        return null;
    }
}