package com.eldersphere.integration;

import com.eldersphere.dtos.Authentication.AuthLoginDTO;
import com.eldersphere.dtos.Authentication.LoginResponseDTO;
import com.eldersphere.dtos.User.DefaultRoleUpdateRequest;
import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.enums.UserTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end multi-role login + switch-default-role flow, exercised against the
 * "caretaker.demo@eldersphere.app" seed account (V34 migration grants it CARETAKER as
 * primary plus FAMILY_MEMBER as a secondary role) - real seed data, not hand-rolled fixtures.
 */
class MultiRoleLoginIntegrationTest extends AbstractIntegrationTest {

    private static final String DEMO_EMAIL = "caretaker.demo@eldersphere.app";
    private static final String DEMO_PASSWORD = "Password123!";

    @Test
    void login_returnsBothHeldRoles_thenSwitchDefaultRole_thenRejectRoleNotHeld() {
        // 1. Login returns every role the account holds, primary = CARETAKER (per V34 seed).
        LoginResponseDTO loginData = login();
        assertThat(loginData.getRoles()).containsExactlyInAnyOrder(UserTypeEnum.CARETAKER, UserTypeEnum.FAMILY_MEMBER);
        assertThat(loginData.getUserType()).isEqualTo(UserTypeEnum.CARETAKER);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(loginData.getToken());

        // 2. GET /users/me/roles agrees with what login returned.
        ResponseEntity<String> myRoles = restTemplate.exchange(
                baseUrl("/api/v1/users/me/roles"), HttpMethod.GET, new HttpEntity<>(authHeaders), String.class);
        assertThat(myRoles.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractData(myRoles, UserRolesResponse.class).getPrimaryRole()).isEqualTo(UserTypeEnum.CARETAKER);

        // 3. Switch default role to FAMILY_MEMBER (a role this account already holds).
        DefaultRoleUpdateRequest switchRequest = DefaultRoleUpdateRequest.builder().roleType(UserTypeEnum.FAMILY_MEMBER).build();
        ResponseEntity<String> switchResponse = restTemplate.exchange(
                baseUrl("/api/v1/users/me/default-role"), HttpMethod.PUT,
                new HttpEntity<>(switchRequest, authHeaders), String.class);
        assertThat(switchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(extractData(switchResponse, UserRolesResponse.class).getPrimaryRole()).isEqualTo(UserTypeEnum.FAMILY_MEMBER);

        // 4. A subsequent login now reflects FAMILY_MEMBER as the primary role.
        LoginResponseDTO afterSwitch = login();
        assertThat(afterSwitch.getUserType()).isEqualTo(UserTypeEnum.FAMILY_MEMBER);
        assertThat(afterSwitch.getRoles()).containsExactlyInAnyOrder(UserTypeEnum.CARETAKER, UserTypeEnum.FAMILY_MEMBER);

        // 5. Switching to a role this account does NOT hold (SUPER_ADMIN) is rejected.
        DefaultRoleUpdateRequest invalidSwitch = DefaultRoleUpdateRequest.builder().roleType(UserTypeEnum.SUPER_ADMIN).build();
        HttpHeaders afterSwitchHeaders = new HttpHeaders();
        afterSwitchHeaders.setBearerAuth(afterSwitch.getToken());
        ResponseEntity<String> rejectedSwitch = restTemplate.exchange(
                baseUrl("/api/v1/users/me/default-role"), HttpMethod.PUT,
                new HttpEntity<>(invalidSwitch, afterSwitchHeaders), String.class);
        assertThat(rejectedSwitch.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Restore original primary role so this test is repeatable / doesn't leak state to others.
        DefaultRoleUpdateRequest restoreRequest = DefaultRoleUpdateRequest.builder().roleType(UserTypeEnum.CARETAKER).build();
        restTemplate.exchange(baseUrl("/api/v1/users/me/default-role"), HttpMethod.PUT,
                new HttpEntity<>(restoreRequest, afterSwitchHeaders), String.class);
    }

    private LoginResponseDTO login() {
        AuthLoginDTO loginRequest = AuthLoginDTO.builder().email(DEMO_EMAIL).password(DEMO_PASSWORD).build();
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/auth/login"), HttpMethod.POST, new HttpEntity<>(loginRequest), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractData(response, LoginResponseDTO.class);
    }
}
