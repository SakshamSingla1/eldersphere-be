package com.eldersphere.dao.navlink;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.NavLink;
import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.repositories.NavLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NavLinkDao implements IDao<NavLink, Long> {

    private final NavLinkRepository navLinkRepository;

    @Override
    public JpaRepository<NavLink, Long> getRepository() {
        return navLinkRepository;
    }

    public NavLink save(NavLink navLink) {
        return navLinkRepository.save(navLink);
    }

    public List<NavLink> findVisibleForUserType(UserTypeEnum userType) {
        return navLinkRepository.findByUserTypeAndStatusOrderByNavIndexAsc(userType, NavLinkStatusEnum.ACTIVE);
    }

    public void deleteById(Long id) {
        navLinkRepository.deleteById(id);
    }
}
