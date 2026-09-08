package com.eldersphere.dtos.Messaging;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationCreateRequest {

    @NotNull(message = "The other participant's user id is required")
    private Long otherUserId;

    /** Optional: link this conversation to a specific booking's context. */
    private Long bookingId;
}
