package com.eldersphere.dtos.Caretaker;

import com.eldersphere.enums.CaretakerVerificationStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaretakerVerificationUpdateRequest {
    @NotNull(message = "Verification status is required")
    private CaretakerVerificationStatusEnum verificationStatus;
}
