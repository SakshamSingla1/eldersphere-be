package com.eldersphere.repositories;

import com.eldersphere.entities.ContactUs;
import com.eldersphere.enums.ContactUsStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {

    @Query("""
            SELECT c FROM ContactUs c
            WHERE (:search IS NULL OR :search = '' OR LOWER(c.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')
                OR LOWER(c.email) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))
            AND (:status IS NULL OR c.status = :status)
            """)
    Page<ContactUs> findByCriteria(@Param("search") String search,
                                    @Param("status") ContactUsStatusEnum status,
                                    Pageable pageable);
}
