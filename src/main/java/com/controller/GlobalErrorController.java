package com.controller;

import com.web.dto.ApiError;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalErrorController implements ErrorController {

    @RequestMapping(path = "/error", produces = "application/json")
    public ResponseEntity<ApiError> handleError(HttpServletRequest req) {
        Object statusObj = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusObj != null ? Integer.parseInt(statusObj.toString()) : 500;
        String path = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String msg = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (msg == null || msg.isBlank()) {
            msg = HttpStatus.valueOf(status).getReasonPhrase();
        }

        String code = switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> (status >= 500) ? "UNEXPECTED" : "ERROR";
        };

        return ResponseEntity.status(status).body(new ApiError(code, msg, path));
    }
}
