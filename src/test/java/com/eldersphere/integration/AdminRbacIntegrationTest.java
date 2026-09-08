package com.eldersphere.integration;

import com.eldersphere.dtos.Authentication.AuthLoginDTO;
import com.eldersphere.dtos.Authentication.LoginResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end check of the fine-grained admin RBAC system (Task 1) against the real
 * V36-seeded demo data: an ADMIN assigned the "Bookings Coordinator" role can reach the
 * endpoints its permission subset covers and is forbidden everywhere else, while a plain
 * ADMIN (no roleId) and a SUPER_ADMIN both keep full, unrestricted access.
 */
class AdminRbacIntegrationTest extends AbstractIntegrationTest {

    private String loginAndGetToken(String email, String password) {
        AuthLoginDTO loginRequest = AuthLoginDTO.builder().email(email).password(password).build();
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/auth/login"), HttpMethod.POST, new HttpEntity<>(loginRequest), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractData(response, LoginResponseDTO.class).getToken();
    }

    private HttpEntity<Void> authEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    void roleAssignedAdmin_canAccessGrantedModule_butForbiddenOutsidePermissionSubset() {
        String token = loginAndGetToken("roleassigned.admin@eldersphere.app", "Password123!");

        // Granted: Bookings Coordinator role includes BOOKINGS_VIEW.
        ResponseEntity<String> bookingsResponse = restTemplate.exchange(
                baseUrl("/api/v1/bookings"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(bookingsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Granted: Bookings Coordinator role includes MEDICAL_RECORDS_VIEW.
        ResponseEntity<String> medicalRecordsResponse = restTemplate.exchange(
                baseUrl("/api/v1/medical-records/elder/90001"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(medicalRecordsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Not granted: Bookings Coordinator has no USERS_VIEW permission.
        ResponseEntity<String> usersResponse = restTemplate.exchange(
                baseUrl("/api/v1/admin/users"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Not granted: no LANDING_MANAGEMENT_VIEW permission either.
        ResponseEntity<String> landingResponse = restTemplate.exchange(
                baseUrl("/api/v1/landing/features"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(landingResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Roles & Permissions remains SUPER_ADMIN-only regardless (unchanged, pre-existing behavior).
        ResponseEntity<String> rolesResponse = restTemplate.exchange(
                baseUrl("/api/v1/roles"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(rolesResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void plainAdmin_withNoRoleIdAssigned_keepsFullLegacyAccessAcrossModules() {
        String token = loginAndGetToken("admin@eldersphere.app", "Password123!");

        ResponseEntity<String> usersResponse = restTemplate.exchange(
                baseUrl("/api/v1/admin/users"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> landingResponse = restTemplate.exchange(
                baseUrl("/api/v1/landing/features"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(landingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> bookingsResponse = restTemplate.exchange(
                baseUrl("/api/v1/bookings"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(bookingsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void superAdmin_alwaysHasFullAccess_evenThoughRolesEndpointIsSuperAdminOnly() {
        String token = loginAndGetToken("superadmin@eldersphere.app", "Password123!");

        ResponseEntity<String> usersResponse = restTemplate.exchange(
                baseUrl("/api/v1/admin/users"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> rolesResponse = restTemplate.exchange(
                baseUrl("/api/v1/roles"), HttpMethod.GET, authEntity(token), String.class);
        assertThat(rolesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
