package com.eldersphere.dao.user;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDao implements IDao<User, Long> {

    private final UserRepository userRepository;

    @Override
    public JpaRepository<User, Long> getRepository() {
        return userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Page<User> findByCriteria(String search, UserTypeEnum userType, UserStatusEnum status, Pageable pageable) {
        return userRepository.findByCriteria(search, userType, status, pageable);
    }

    public long countByUserType(UserTypeEnum userType) {
        return userRepository.countByUserType(userType);
    }

    public long countByUserTypeAndStatus(UserTypeEnum userType, UserStatusEnum status) {
        return userRepository.countByUserTypeAndStatus(userType, status);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
