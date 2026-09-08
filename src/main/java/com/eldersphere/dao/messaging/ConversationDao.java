package com.eldersphere.dao.messaging;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Conversation;
import com.eldersphere.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConversationDao implements IDao<Conversation, Long> {

    private final ConversationRepository conversationRepository;

    @Override
    public JpaRepository<Conversation, Long> getRepository() {
        return conversationRepository;
    }

    public Conversation save(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public Optional<Conversation> findBetweenUsers(Long userId1, Long userId2, Long bookingId) {
        return conversationRepository.findBetweenUsers(userId1, userId2, bookingId);
    }

    public Page<Conversation> findForUser(Long userId, Pageable pageable) {
        return conversationRepository.findForUser(userId, pageable);
    }
}
