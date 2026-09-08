package com.eldersphere.dtos.Messaging;

import com.eldersphere.dtos.Common.AuditableResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationResponse extends AuditableResponse {
    private Long id;
    private Long userAId;
    private Long userBId;
    private Long otherUserId;
    private String otherUserName;
    private Long bookingId;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
