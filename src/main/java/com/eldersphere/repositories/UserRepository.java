package com.eldersphere.repositories;

import com.eldersphere.entities.User;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                OR LOWER(u.email) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))
            AND (:userType IS NULL OR u.userType = :userType)
            AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> findByCriteria(@Param("search") String search,
                               @Param("userType") UserTypeEnum userType,
                               @Param("status") UserStatusEnum status,
                               Pageable pageable);

    long countByUserType(UserTypeEnum userType);

    long countByUserTypeAndStatus(UserTypeEnum userType, UserStatusEnum status);
}
