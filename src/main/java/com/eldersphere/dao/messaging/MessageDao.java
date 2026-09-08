package com.eldersphere.dao.messaging;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Message;
import com.eldersphere.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MessageDao implements IDao<Message, Long> {

    private final MessageRepository messageRepository;

    @Override
    public JpaRepository<Message, Long> getRepository() {
        return messageRepository;
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public Page<Message> findByConversationId(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable);
    }

    public long countUnreadInConversationForUser(Long conversationId, Long userId) {
        return messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(conversationId, userId);
    }

    public int markConversationRead(Long conversationId, Long userId) {
        return messageRepository.markConversationRead(conversationId, userId, LocalDateTime.now());
    }
}
