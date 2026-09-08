package com.eldersphere.utils;

import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.repositories.UserRepository;
import com.eldersphere.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Helper {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public String extractEmailFromHeader(String header) throws GenericException {
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.extractEmail(header.substring(7));
        }
        return getAuthenticatedEmail();
    }

    public User getUserFromHeader(String header) throws GenericException {
        if (header != null && header.startsWith("Bearer ")) {
            String email = jwtUtil.extractEmail(header.substring(7));
            if (email != null) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found"));
            }
        }
        return getAuthenticatedUser();
    }

    public Long getUserIdFromHeader(String header) throws GenericException {
        if (header != null && header.startsWith("Bearer ")) {
            String userId = jwtUtil.extractUserId(header.substring(7));
            if (userId != null) return Long.parseLong(userId);
        }
        return getAuthenticatedUserId();
    }

    public Long getAuthenticatedUserId() throws GenericException {
        return getAuthenticatedUser().getId();
    }

    public User getAuthenticatedUser() throws GenericException {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found"));
    }

    private String getAuthenticatedEmail() throws GenericException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new GenericException(ExceptionCodeEnum.UNAUTHORIZED, "Not authenticated");
        }
        return auth.getName();
    }
}
