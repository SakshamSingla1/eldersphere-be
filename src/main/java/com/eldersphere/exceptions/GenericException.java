package com.eldersphere.exceptions;

import com.eldersphere.enums.ExceptionCodeEnum;
import lombok.*;
import org.springframework.web.bind.annotation.ControllerAdvice;

@RequiredArgsConstructor
@ControllerAdvice
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Data
public class GenericException extends Exception {
    public ExceptionCodeEnum exceptionCode;
    public String errorMessage;
    public String referenceId;

    public GenericException(ExceptionCodeEnum exceptionCode, String errorMessage) {
        super(errorMessage);
        this.exceptionCode = exceptionCode;
        this.errorMessage = errorMessage;
    }
}
