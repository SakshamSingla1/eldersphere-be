package com.eldersphere.services;

import com.eldersphere.dtos.NavLink.NavLinkRequest;
import com.eldersphere.dtos.NavLink.NavLinkResponse;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NavLinkService {
    NavLinkResponse create(NavLinkRequest request) throws GenericException;
    NavLinkResponse update(Long id, NavLinkRequest request) throws GenericException;
    NavLinkResponse getById(Long id) throws GenericException;
    Page<NavLinkResponse> getAll(Pageable pageable);
    void delete(Long id) throws GenericException;

    /** Resolves the caller's own portal's sidebar: active items for their user type, filtered
     * by super-admin-only and required-permission gates exactly as the underlying routes
     * already enforce, ordered for direct rendering. */
    List<NavLinkResponse> getMine(User caller);
}
