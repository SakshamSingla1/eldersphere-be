package com.eldersphere.security;

import com.eldersphere.entities.Role;
import com.eldersphere.entities.User;
import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.repositories.RoleRepository;
import com.eldersphere.repositories.UserRepository;
import com.eldersphere.repositories.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // One authority per role the user holds (multi-role support), sourced fresh from the
        // DB on every request so a role grant/revoke takes effect immediately.
        Set<String> roleNames = new LinkedHashSet<>();
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByUserId(user.getId());
        for (UserRoleMapping mapping : mappings) {
            roleNames.add(mapping.getRoleType().name());
        }
        if (roleNames.isEmpty()) {
            // Safety net for a user with no mapping row yet (e.g. pre-migration edge case).
            roleNames.add(user.getUserType() != null ? user.getUserType().name() : "FAMILY_MEMBER");
        }

        // Pre-existing, unrelated fine-grained admin-panel RBAC: if this user has been assigned
        // a custom Role (nav-item permissions), grant that as an additional authority too rather
        // than replacing the userType-derived ones above.
        if (user.getRoleId() != null) {
            Role role = roleRepository.findById(user.getRoleId()).orElse(null);
            if (role != null && role.getName() != null) {
                roleNames.add(role.getName());
            }
        }

        List<GrantedAuthority> authorities = roleNames.stream()
                .map(name -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + name))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPasswordHash(), authorities);
    }
}
