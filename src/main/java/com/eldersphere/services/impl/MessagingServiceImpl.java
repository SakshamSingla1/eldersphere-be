package com.eldersphere.services.impl;

import com.eldersphere.dao.messaging.ConversationDao;
import com.eldersphere.dao.messaging.MessageDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Messaging.ConversationCreateRequest;
import com.eldersphere.dtos.Messaging.ConversationResponse;
import com.eldersphere.dtos.Messaging.MessageRequest;
import com.eldersphere.dtos.Messaging.MessageResponse;
import com.eldersphere.entities.Conversation;
import com.eldersphere.entities.Message;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NotificationTypeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.MessagingService;
import com.eldersphere.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {

    private final ConversationDao conversationDao;
    private final MessageDao messageDao;
    private final UserDao userDao;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(Long callerId, ConversationCreateRequest request) throws GenericException {
        Long otherUserId = request.getOtherUserId();
        if (otherUserId.equals(callerId)) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "Cannot start a conversation with yourself");
        }
        User other = userDao.findById(otherUserId, true);
        if (other == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }

        Conversation conversation = conversationDao.findBetweenUsers(callerId, otherUserId, request.getBookingId())
                .orElseGet(() -> conversationDao.save(Conversation.builder()
                        .userAId(callerId)
                        .userBId(otherUserId)
                        .bookingId(request.getBookingId())
                        .build()));

        return toResponse(conversation, callerId);
    }

    @Override
    public Page<ConversationResponse> getConversationsForUser(Long callerId, Pageable pageable) {
        return conversationDao.findForUser(callerId, pageable).map(c -> toResponse(c, callerId));
    }

    @Override
    public Page<MessageResponse> getMessages(Long callerId, Long conversationId, Pageable pageable) throws GenericException {
        Conversation conversation = getConversationOrThrow(conversationId);
        assertAccess(conversation, callerId);
        return messageDao.findByConversationId(conversationId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long callerId, Long conversationId, MessageRequest request) throws GenericException {
        Conversation conversation = getConversationOrThrow(conversationId);
        assertAccess(conversation, callerId);

        LocalDateTime now = LocalDateTime.now();
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(callerId)
                .content(request.getContent())
                .sentAt(now)
                .build();
        message = messageDao.save(message);

        conversation.setLastMessageAt(now);
        conversationDao.save(conversation);

        MessageResponse response = toResponse(message);

        try {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, response);
        } catch (Exception e) {
            log.warn("Failed to broadcast message {} on conversation {}: {}", message.getId(), conversationId, e.getMessage());
        }

        Long recipientId = conversation.getUserAId().equals(callerId) ? conversation.getUserBId() : conversation.getUserAId();
        User sender = userDao.findById(callerId, true);
        String senderName = sender != null ? sender.getFullName() : "Someone";
        notificationService.create(recipientId, NotificationTypeEnum.NEW_MESSAGE,
                "New message from " + senderName,
                request.getContent().length() > 140 ? request.getContent().substring(0, 140) + "..." : request.getContent(),
                "/conversations/" + conversationId);

        return response;
    }

    @Override
    @Transactional
    public void markRead(Long callerId, Long conversationId) throws GenericException {
        Conversation conversation = getConversationOrThrow(conversationId);
        assertAccess(conversation, callerId);
        messageDao.markConversationRead(conversationId, callerId);
    }

    private Conversation getConversationOrThrow(Long conversationId) throws GenericException {
        Conversation conversation = conversationDao.findById(conversationId, true);
        if (conversation == null) {
            throw new GenericException(ExceptionCodeEnum.CONVERSATION_NOT_FOUND, "Conversation not found");
        }
        return conversation;
    }

    private void assertAccess(Conversation conversation, Long callerId) throws GenericException {
        if (conversation.getUserAId().equals(callerId) || conversation.getUserBId().equals(callerId)) {
            return;
        }
        User caller = userDao.findById(callerId, true);
        boolean isAdmin = caller != null && (caller.getUserType() == UserTypeEnum.ADMIN || caller.getUserType() == UserTypeEnum.SUPER_ADMIN);
        if (!isAdmin) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "You are not a participant of this conversation");
        }
    }

    private ConversationResponse toResponse(Conversation conversation, Long callerId) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setUserAId(conversation.getUserAId());
        response.setUserBId(conversation.getUserBId());
        Long otherUserId = conversation.getUserAId().equals(callerId) ? conversation.getUserBId() : conversation.getUserAId();
        response.setOtherUserId(otherUserId);
        User other = userDao.findById(otherUserId, true);
        if (other != null) response.setOtherUserName(other.getFullName());
        response.setBookingId(conversation.getBookingId());
        response.setLastMessageAt(conversation.getLastMessageAt());
        response.setUnreadCount(messageDao.countUnreadInConversationForUser(conversation.getId(), callerId));
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        return response;
    }

    private MessageResponse toResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversationId());
        response.setSenderId(message.getSenderId());
        User sender = userDao.findById(message.getSenderId(), true);
        if (sender != null) response.setSenderName(sender.getFullName());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setReadAt(message.getReadAt());
        return response;
    }
}
