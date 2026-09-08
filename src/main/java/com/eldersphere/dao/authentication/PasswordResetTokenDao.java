package com.eldersphere.dao.authentication;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.PasswordResetToken;
import com.eldersphere.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenDao implements IDao<PasswordResetToken, Long> {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public JpaRepository<PasswordResetToken, Long> getRepository() {
        return passwordResetTokenRepository;
    }

    public PasswordResetToken save(PasswordResetToken token) {
        return passwordResetTokenRepository.save(token);
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public void deleteByUserId(Long userId) {
        passwordResetTokenRepository.deleteByUserId(userId);
    }
}
