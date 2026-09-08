package com.eldersphere.services;

import com.eldersphere.dtos.Messaging.ConversationCreateRequest;
import com.eldersphere.dtos.Messaging.ConversationResponse;
import com.eldersphere.dtos.Messaging.MessageRequest;
import com.eldersphere.dtos.Messaging.MessageResponse;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessagingService {

    ConversationResponse getOrCreateConversation(Long callerId, ConversationCreateRequest request) throws GenericException;

    Page<ConversationResponse> getConversationsForUser(Long callerId, Pageable pageable) throws GenericException;

    Page<MessageResponse> getMessages(Long callerId, Long conversationId, Pageable pageable) throws GenericException;

    MessageResponse sendMessage(Long callerId, Long conversationId, MessageRequest request) throws GenericException;

    void markRead(Long callerId, Long conversationId) throws GenericException;
}
