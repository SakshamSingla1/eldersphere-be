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

import java.util.List;
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

    // Used by the "search & link" flow (family member searching for an existing elder or
    // co-family-member to invite) — unlike findByCriteria (admin panel) this also matches
    // phone, and is deliberately capped by the caller's Pageable rather than exposing a
    // full paginated browse, since this is a "find one specific person" lookup, not a listing.
    @Query("""
            SELECT u FROM User u
            WHERE (LOWER(u.fullName) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                OR LOWER(u.email) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                OR u.phone LIKE CONCAT('%', CAST(:search AS string), '%'))
            AND u.userType IN :types
            AND u.status = com.eldersphere.enums.UserStatusEnum.ACTIVE
            AND u.id <> :excludeUserId
            """)
    List<User> searchLinkable(@Param("search") String search,
                               @Param("excludeUserId") Long excludeUserId,
                               @Param("types") List<UserTypeEnum> types,
                               Pageable pageable);
}
