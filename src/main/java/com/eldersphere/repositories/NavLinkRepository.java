package com.eldersphere.repositories;

import com.eldersphere.entities.NavLink;
import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavLinkRepository extends JpaRepository<NavLink, Long> {
    List<NavLink> findByUserTypeAndStatusOrderByNavIndexAsc(UserTypeEnum userType, NavLinkStatusEnum status);
}
