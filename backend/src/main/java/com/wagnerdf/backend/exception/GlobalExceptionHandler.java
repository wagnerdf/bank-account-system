package com.wagnerdf.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.wagnerdf.backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===============================
    // IllegalArgumentException -> 400
    // ===============================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return buildResponse(status, ex.getMessage(), request);
    }

    // ===============================
    // IllegalStateException -> 409
    // ===============================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.CONFLICT;

        return buildResponse(status, ex.getMessage(), request);
    }

    // ============================================
    // Bean Validation (@NotNull, @Positive, etc)
    // ============================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return buildResponse(status, message, request);
    }

    // =================================================
    // JSON inválido / Enum inválido / campo mal formatado
    // =================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Corpo da solicitação inválido.";

        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {

            if (!invalidFormat.getPath().isEmpty()) {
                String fieldName = invalidFormat.getPath().get(0).getFieldName();
                message = "Valor inválido para o campo: " + fieldName;
            }
        }

        return buildResponse(status, message, request);
    }

    // ===============================
    // BusinessException customizada
    // ===============================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                ex.getStatus(),
                ex.getMessage(),
                request
        );
    }

    // ===============================
    // Fallback -> 500
    // ===============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return buildResponse(status, "Unexpected error occurred", request);
    }

    // ===============================
    // Método auxiliar para padronizar
    // ===============================
    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
