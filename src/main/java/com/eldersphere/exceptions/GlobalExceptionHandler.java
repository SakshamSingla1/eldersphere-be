package com.eldersphere.exceptions;

import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.payload.ResponseModel;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Set<ExceptionCodeEnum> UNAUTHORIZED_CODES = Set.of(
            ExceptionCodeEnum.UNAUTHORIZED,
            ExceptionCodeEnum.INVALID_CREDENTIALS,
            ExceptionCodeEnum.TOKEN_EXPIRED,
            ExceptionCodeEnum.TOKEN_INVALID
    );

    private static final Set<ExceptionCodeEnum> BAD_REQUEST_CODES = Set.of(
            ExceptionCodeEnum.BAD_REQUEST,
            ExceptionCodeEnum.INVALID_ARGUMENT,
            ExceptionCodeEnum.VALIDATION_FAILED,
            ExceptionCodeEnum.PASSWORD_TOO_WEAK,
            ExceptionCodeEnum.PASSWORD_MISMATCH,
            ExceptionCodeEnum.PASSWORD_RESET_FAILED,
            ExceptionCodeEnum.FORMAT_ERROR,
            ExceptionCodeEnum.INVALID_BOOKING_STATUS_TRANSITION,
            ExceptionCodeEnum.BOOKING_CONFLICT,
            ExceptionCodeEnum.CARETAKER_UNAVAILABLE,
            ExceptionCodeEnum.USER_ROLE_NOT_HELD,
            ExceptionCodeEnum.CANNOT_DELETE_DEFAULT_COLOR_THEME,
            ExceptionCodeEnum.COLOR_THEME_INACTIVE
    );

    private static final Set<ExceptionCodeEnum> CONFLICT_CODES = Set.of(
            ExceptionCodeEnum.DUPLICATE_EMAIL,
            ExceptionCodeEnum.ADMIN_ALREADY_EXISTS,
            ExceptionCodeEnum.DUPLICATE_ROLE,
            ExceptionCodeEnum.DUPLICATE_PERMISSION,
            ExceptionCodeEnum.DUPLICATE_ROLE_PERMISSION,
            ExceptionCodeEnum.DUPLICATE_COLOR_THEME,
            ExceptionCodeEnum.DUPLICATE_CARETAKER_PROFILE,
            ExceptionCodeEnum.DUPLICATE_REVIEW,
            ExceptionCodeEnum.DUPLICATE_REVIEW_REPLY,
            ExceptionCodeEnum.DUPLICATE_FAVORITE_CARETAKER,
            ExceptionCodeEnum.USER_ROLE_ALREADY_ASSIGNED,
            ExceptionCodeEnum.LAST_USER_ROLE_CANNOT_BE_REVOKED,
            ExceptionCodeEnum.PRIMARY_USER_ROLE_CANNOT_BE_REVOKED
    );

    private static final Set<ExceptionCodeEnum> NOT_FOUND_CODES = Set.of(
            ExceptionCodeEnum.USER_NOT_FOUND,
            ExceptionCodeEnum.ROLE_NOT_FOUND,
            ExceptionCodeEnum.PERMISSION_NOT_FOUND,
            ExceptionCodeEnum.ROLE_PERMISSION_NOT_FOUND,
            ExceptionCodeEnum.COLOR_THEME_NOT_FOUND,
            ExceptionCodeEnum.CARETAKER_PROFILE_NOT_FOUND,
            ExceptionCodeEnum.ELDER_PROFILE_NOT_FOUND,
            ExceptionCodeEnum.SERVICE_OFFERING_NOT_FOUND,
            ExceptionCodeEnum.BOOKING_NOT_FOUND,
            ExceptionCodeEnum.MEDICAL_RECORD_NOT_FOUND,
            ExceptionCodeEnum.REVIEW_NOT_FOUND,
            ExceptionCodeEnum.EMERGENCY_ALERT_NOT_FOUND,
            ExceptionCodeEnum.NOTIFICATION_NOT_FOUND,
            ExceptionCodeEnum.LANDING_FEATURE_NOT_FOUND,
            ExceptionCodeEnum.LANDING_FAQ_NOT_FOUND,
            ExceptionCodeEnum.LANDING_TESTIMONIAL_NOT_FOUND,
            ExceptionCodeEnum.CONTACT_US_NOT_FOUND,
            ExceptionCodeEnum.FILE_NOT_FOUND,
            ExceptionCodeEnum.DATA_NOT_FOUND,
            ExceptionCodeEnum.CONVERSATION_NOT_FOUND,
            ExceptionCodeEnum.MESSAGE_NOT_FOUND,
            ExceptionCodeEnum.CARETAKER_AVAILABILITY_NOT_FOUND,
            ExceptionCodeEnum.REVIEW_REPLY_NOT_FOUND,
            ExceptionCodeEnum.FAVORITE_CARETAKER_NOT_FOUND,
            ExceptionCodeEnum.USER_ROLE_NOT_ASSIGNED
    );

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ResponseModel<Void>> handleGenericException(GenericException ex) {
        ExceptionCodeEnum code = ex.getExceptionCode();
        String message = ex.getErrorMessage() != null ? ex.getErrorMessage() : "An error occurred";
        log.warn("GenericException [{}]: {}", code, message);

        HttpStatus status = resolveStatus(code);
        ResponseModel<Void> body = new ResponseModel<>(message, null, code != null ? code.getValue() : "ERROR");
        body.toFailure();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ResponseModel<Void>> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        log.warn("InvalidDataAccessApiUsageException: {}", ex.getMessage());
        ResponseModel<Void> body = new ResponseModel<>("Invalid query parameter. Check your sort or filter values.", null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseModel<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = "Authorization header is required";
        log.warn("MissingRequestHeaderException: {}", ex.getHeaderName());
        ResponseModel<Void> body = new ResponseModel<>(message, null, ExceptionCodeEnum.UNAUTHORIZED.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseModel<Void>> handleMissingParameter(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        log.warn("MissingServletRequestParameterException: {}", ex.getMessage());
        String message = "Required query parameter '" + ex.getParameterName() + "' is missing.";
        ResponseModel<Void> body = new ResponseModel<>(message, null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ResponseModel<Void>> handleInvalidDataAccessResource(InvalidDataAccessResourceUsageException ex) {
        log.warn("InvalidDataAccessResourceUsageException: {}", ex.getMessage());
        ResponseModel<Void> body = new ResponseModel<>("Invalid query. Check your search or filter values.", null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        java.util.List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String message = "Validation failed: " + String.join(", ", errors);
        log.warn("ValidationException: {}", message);

        ResponseModel<Void> body = new ResponseModel<>(message, null, ExceptionCodeEnum.VALIDATION_FAILED.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseModel<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("ConstraintViolationException: {}", message);
        ResponseModel<Void> body = new ResponseModel<>("Validation failed: " + message, null, ExceptionCodeEnum.VALIDATION_FAILED.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseModel<Void>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        log.warn("MethodArgumentTypeMismatchException: {}", message);
        ResponseModel<Void> body = new ResponseModel<>(message, null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseModel<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        ResponseModel<Void> body = new ResponseModel<>("Malformed JSON request body", null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseModel<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("HttpRequestMethodNotSupportedException: {}", ex.getMethod());
        ResponseModel<Void> body = new ResponseModel<>("HTTP method " + ex.getMethod() + " is not supported for this endpoint", null, ExceptionCodeEnum.BAD_REQUEST.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseModel<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        ResponseModel<Void> body = new ResponseModel<>("You do not have permission to perform this action.", null,
                ExceptionCodeEnum.FORBIDDEN.getValue());
        body.toFailure();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ResponseModel<Void> body = new ResponseModel<>("An unexpected error occurred. Please try again.", null,
                ExceptionCodeEnum.INTERNAL_SERVER_ERROR.getValue());
        body.toInternalServerError();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus resolveStatus(ExceptionCodeEnum code) {
        if (code == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (UNAUTHORIZED_CODES.contains(code)) return HttpStatus.UNAUTHORIZED;
        if (code == ExceptionCodeEnum.FORBIDDEN) return HttpStatus.FORBIDDEN;
        if (NOT_FOUND_CODES.contains(code)) return HttpStatus.NOT_FOUND;
        if (BAD_REQUEST_CODES.contains(code)) return HttpStatus.BAD_REQUEST;
        if (CONFLICT_CODES.contains(code)) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
