package com.eldersphere.integration;

import com.eldersphere.dtos.Authentication.AuthLoginDTO;
import com.eldersphere.dtos.Authentication.AuthRegisterDTO;
import com.eldersphere.dtos.Authentication.LoginResponseDTO;
import com.eldersphere.enums.UserTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end auth flow against a real (Testcontainers) Postgres + the full Spring context:
 * register a brand-new account, log in with it, use the JWT to access a protected endpoint,
 * and confirm an unauthenticated/under-privileged caller is rejected.
 */
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void register_thenLogin_thenAccessProtectedEndpoint_thenRejectUnauthorized() {
        String uniqueEmail = "auth-flow-" + System.nanoTime() + "@eldersphere.test";

        // 1. Register a new FAMILY_MEMBER account.
        AuthRegisterDTO registerRequest = AuthRegisterDTO.builder()
                .fullName("Integration Test User")
                .email(uniqueEmail)
                .phone("+15550001111")
                .password("Password123!")
                .userType(UserTypeEnum.FAMILY_MEMBER)
                .build();

        ResponseEntity<String> registerResponse = restTemplate.exchange(
                baseUrl("/api/v1/auth/register"), HttpMethod.POST, new HttpEntity<>(registerRequest), String.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Log in with the freshly registered account.
        AuthLoginDTO loginRequest = AuthLoginDTO.builder().email(uniqueEmail).password("Password123!").build();
        ResponseEntity<String> loginResponse = restTemplate.exchange(
                baseUrl("/api/v1/auth/login"), HttpMethod.POST, new HttpEntity<>(loginRequest), String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponseDTO loginData = extractData(loginResponse, LoginResponseDTO.class);
        assertThat(loginData.getToken()).isNotBlank();
        assertThat(loginData.getRoles()).containsExactly(UserTypeEnum.FAMILY_MEMBER);

        // 3. Use the token to access a protected, authenticated-only endpoint (own roles).
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(loginData.getToken());
        ResponseEntity<String> myRolesResponse = restTemplate.exchange(
                baseUrl("/api/v1/users/me/roles"), HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertThat(myRolesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4a. No token at all -> unauthorized on an authenticated-only endpoint.
        ResponseEntity<String> noTokenResponse = restTemplate.getForEntity(baseUrl("/api/v1/users/me/roles"), String.class);
        assertThat(noTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 4b. Valid token, but insufficient privilege (FAMILY_MEMBER hitting an admin-only endpoint) -> forbidden.
        ResponseEntity<String> forbiddenResponse = restTemplate.exchange(
                baseUrl("/api/v1/admin/users"), HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertThat(forbiddenResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() {
        AuthLoginDTO loginRequest = AuthLoginDTO.builder()
                .email("caretaker.demo@eldersphere.app")
                .password("definitely-wrong-password")
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/auth/login"), HttpMethod.POST, new HttpEntity<>(loginRequest), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
