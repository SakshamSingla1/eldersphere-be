package com.eldersphere.dao.user;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.repositories.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRoleMappingDao implements IDao<UserRoleMapping, Long> {

    private final UserRoleMappingRepository userRoleMappingRepository;

    @Override
    public JpaRepository<UserRoleMapping, Long> getRepository() {
        return userRoleMappingRepository;
    }

    public UserRoleMapping save(UserRoleMapping mapping) {
        return userRoleMappingRepository.save(mapping);
    }

    public List<UserRoleMapping> findByUserId(Long userId) {
        return userRoleMappingRepository.findByUserId(userId);
    }

    public Optional<UserRoleMapping> find(Long userId, UserTypeEnum roleType) {
        return userRoleMappingRepository.findByUserIdAndRoleType(userId, roleType);
    }

    public boolean exists(Long userId, UserTypeEnum roleType) {
        return userRoleMappingRepository.existsByUserIdAndRoleType(userId, roleType);
    }

    public long countByUserId(Long userId) {
        return userRoleMappingRepository.countByUserId(userId);
    }

    public void delete(UserRoleMapping mapping) {
        userRoleMappingRepository.delete(mapping);
    }
}
