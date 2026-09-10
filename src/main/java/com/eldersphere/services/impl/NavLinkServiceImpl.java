package com.eldersphere.services.impl;

import com.eldersphere.dao.navlink.NavLinkDao;
import com.eldersphere.dtos.NavLink.NavLinkRequest;
import com.eldersphere.dtos.NavLink.NavLinkResponse;
import com.eldersphere.entities.NavLink;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.security.AdminPermissionGuard;
import com.eldersphere.services.NavLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NavLinkServiceImpl implements NavLinkService {

    private final NavLinkDao navLinkDao;
    private final AdminPermissionGuard adminPermissionGuard;

    @Override
    @Transactional
    public NavLinkResponse create(NavLinkRequest request) throws GenericException {
        NavLink navLink = NavLink.builder()
                .userType(request.getUserType())
                .navGroup(request.getNavGroup())
                .navIndex(request.getNavIndex())
                .name(request.getName())
                .path(request.getPath())
                .icon(request.getIcon())
                .requiredPermission(request.getRequiredPermission())
                .superAdminOnly(Boolean.TRUE.equals(request.getSuperAdminOnly()))
                .status(request.getStatus() != null ? request.getStatus() : NavLinkStatusEnum.ACTIVE)
                .build();
        return toResponse(navLinkDao.save(navLink));
    }

    @Override
    @Transactional
    public NavLinkResponse update(Long id, NavLinkRequest request) throws GenericException {
        NavLink navLink = navLinkDao.findById(id, true);
        if (navLink == null) {
            throw new GenericException(ExceptionCodeEnum.NAV_LINK_NOT_FOUND, "Navigation link not found");
        }
        navLink.setUserType(request.getUserType());
        navLink.setNavGroup(request.getNavGroup());
        navLink.setNavIndex(request.getNavIndex());
        navLink.setName(request.getName());
        navLink.setPath(request.getPath());
        navLink.setIcon(request.getIcon());
        navLink.setRequiredPermission(request.getRequiredPermission());
        navLink.setSuperAdminOnly(Boolean.TRUE.equals(request.getSuperAdminOnly()));
        if (request.getStatus() != null) {
            navLink.setStatus(request.getStatus());
        }
        return toResponse(navLinkDao.save(navLink));
    }

    @Override
    public NavLinkResponse getById(Long id) throws GenericException {
        NavLink navLink = navLinkDao.findById(id, true);
        if (navLink == null) {
            throw new GenericException(ExceptionCodeEnum.NAV_LINK_NOT_FOUND, "Navigation link not found");
        }
        return toResponse(navLink);
    }

    @Override
    public Page<NavLinkResponse> getAll(Pageable pageable) {
        return navLinkDao.getRepository().findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        NavLink navLink = navLinkDao.findById(id, true);
        if (navLink == null) {
            throw new GenericException(ExceptionCodeEnum.NAV_LINK_NOT_FOUND, "Navigation link not found");
        }
        navLinkDao.deleteById(id);
    }

    @Override
    public List<NavLinkResponse> getMine(User caller) {
        // The Admin portal's sidebar is shared by ADMIN and SUPER_ADMIN alike (see
        // AdminRoutes.tsx's isSuperAdmin flag layered on one item list) — SUPER_ADMIN has no
        // nav_links rows of its own, it reads the ADMIN portal's rows and additionally clears
        // every super_admin_only gate.
        boolean isSuperAdmin = caller.getUserType() == UserTypeEnum.SUPER_ADMIN;
        UserTypeEnum lookupType = isSuperAdmin ? UserTypeEnum.ADMIN : caller.getUserType();

        return navLinkDao.findVisibleForUserType(lookupType).stream()
                .filter(link -> isSuperAdmin || !Boolean.TRUE.equals(link.getSuperAdminOnly()))
                .filter(link -> link.getRequiredPermission() == null || adminPermissionGuard.has(link.getRequiredPermission()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NavLinkResponse toResponse(NavLink navLink) {
        NavLinkResponse response = new NavLinkResponse();
        response.setId(navLink.getId());
        response.setUserType(navLink.getUserType());
        response.setNavGroup(navLink.getNavGroup());
        response.setNavIndex(navLink.getNavIndex());
        response.setName(navLink.getName());
        response.setPath(navLink.getPath());
        response.setIcon(navLink.getIcon());
        response.setRequiredPermission(navLink.getRequiredPermission());
        response.setSuperAdminOnly(navLink.getSuperAdminOnly());
        response.setStatus(navLink.getStatus());
        response.setCreatedAt(navLink.getCreatedAt());
        response.setUpdatedAt(navLink.getUpdatedAt());
        return response;
    }
}
