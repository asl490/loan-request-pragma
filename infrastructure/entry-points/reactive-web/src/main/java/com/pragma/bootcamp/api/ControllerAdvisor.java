package com.pragma.bootcamp.api;

import com.pragma.bootcamp.api.dto.ErrorResponse;
import com.pragma.bootcamp.exception.NotFoundException;
import com.pragma.bootcamp.exception.BusinessException;
import com.pragma.bootcamp.exception.DataIntegrityViolationException;
import com.pragma.bootcamp.exception.ValidationException;
import com.pragma.bootcamp.model.loantype.exception.LoanAmountOutOfRangeException;
import com.pragma.bootcamp.model.loantype.exception.LoanTypeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerAdvisor {

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.NOT_FOUND.name())
                                                .message(ex.getMessage())
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.BAD_REQUEST.name())
                                                .message(ex.getMessage())
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.CONFLICT.name())
                                                .message(ex.getMessage())
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.CONFLICT);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.BAD_REQUEST.name())
                                                .message("Error de integridad de datos: " + getMessage(ex))
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.BAD_REQUEST.name())
                                                .message(ex.getMessage())
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.BAD_REQUEST);
        }

    @ExceptionHandler(LoanTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLoanTypeNotFoundException(LoanTypeNotFoundException ex) {
        return new ResponseEntity<>(
                ErrorResponse.builder()

                        .errorCode(HttpStatus.BAD_REQUEST.name())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LoanAmountOutOfRangeException.class)
    public ResponseEntity<ErrorResponse> handleLoanAmountOutOfRangeException(LoanAmountOutOfRangeException ex) {
        return new ResponseEntity<>(
                ErrorResponse.builder()

                        .errorCode(HttpStatus.BAD_REQUEST.name())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                return new ResponseEntity<>(
                                ErrorResponse.builder()

                                                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                                                .message("An unexpected error occurred")
                                                .timestamp(LocalDateTime.now())
                                                .errors(null)
                                                .build(),
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private String getMessage(Throwable ex) {
                Throwable cause = ex;
                while (cause.getCause() != null) {
                        cause = cause.getCause();
                }
                return cause.getMessage();
        }
}