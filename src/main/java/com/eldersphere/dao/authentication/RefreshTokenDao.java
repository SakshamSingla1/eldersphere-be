package com.eldersphere.dao.authentication;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.RefreshToken;
import com.eldersphere.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenDao implements IDao<RefreshToken, Long> {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public JpaRepository<RefreshToken, Long> getRepository() {
        return refreshTokenRepository;
    }

    public RefreshToken save(RefreshToken token) {
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
