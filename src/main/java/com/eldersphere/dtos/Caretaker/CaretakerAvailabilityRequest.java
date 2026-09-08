package com.eldersphere.dtos.Caretaker;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Replace-all request: the submitted list of slots becomes the caretaker's entire
 * weekly availability, replacing whatever was previously stored.
 */
@Data
public class CaretakerAvailabilityRequest {

    @NotNull(message = "Slots list is required (send an empty list to clear availability)")
    @Valid
    private List<AvailabilitySlotRequest> slots;
}
