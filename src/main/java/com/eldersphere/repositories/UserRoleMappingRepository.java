package com.eldersphere.repositories;

import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.enums.UserTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {

    List<UserRoleMapping> findByUserId(Long userId);

    Optional<UserRoleMapping> findByUserIdAndRoleType(Long userId, UserTypeEnum roleType);

    boolean existsByUserIdAndRoleType(Long userId, UserTypeEnum roleType);

    long countByUserId(Long userId);

    void deleteByUserIdAndRoleType(Long userId, UserTypeEnum roleType);
}
