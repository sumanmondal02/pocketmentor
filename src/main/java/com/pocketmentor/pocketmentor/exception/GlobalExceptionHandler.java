package com.pocketmentor.pocketmentor.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatusCode s, String msg) {
        return ResponseEntity.status(s).body(Map.of("status", s.value(), "error", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> badJson(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "Malformed or missing request body");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> tooBig(MaxUploadSizeExceededException e) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "File too large (max 10 MB)");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException e) {
        log.warn("Data integrity: {}", e.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    // Handles ResponseStatusException and Spring's own web errors (404, 405, missing params...)
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> any(Exception e) {
        if (e instanceof ErrorResponse er) {
            String detail = er.getBody().getDetail();
            if (detail == null || detail.isBlank()) {
                detail = HttpStatus.valueOf(er.getStatusCode().value()).getReasonPhrase();
            }
            return body(er.getStatusCode(), detail);
        }
        log.error("Unexpected error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }
}