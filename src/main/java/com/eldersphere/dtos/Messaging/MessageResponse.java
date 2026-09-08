package com.eldersphere.dtos.Messaging;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
