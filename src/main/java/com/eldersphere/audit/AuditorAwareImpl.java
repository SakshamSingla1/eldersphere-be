package com.eldersphere.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String currentUserId = (String) request.getAttribute("currentUserId");
                if (currentUserId != null) {
                    return Optional.of(Long.parseLong(currentUserId));
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get user ID from request: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
