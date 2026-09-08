package com.eldersphere.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JWT generation/parsing, including the multi-role "roles" claim added
 * alongside the existing single-value "role" (primary role) claim.
 */
class JwtUtilTest {

    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1lbGRlcnNwaGVyZS1qd3QtdW5pdC10ZXN0cyE=";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3_600_000L);
    }

    @Test
    void generateAccessToken_roundTripsEmailAndUserId() {
        String token = jwtUtil.generateAccessToken("family.demo@eldersphere.app", "90004");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("family.demo@eldersphere.app");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("90004");
    }

    @Test
    void generateAccessToken_withRoles_roundTripsMultiRoleClaim() {
        String token = jwtUtil.generateAccessToken(
                "family.demo@eldersphere.app", "90004", "FAMILY_MEMBER", List.of("FAMILY_MEMBER", "ELDER"));

        assertThat(jwtUtil.extractRoles(token)).containsExactlyInAnyOrder("FAMILY_MEMBER", "ELDER");
    }

    @Test
    void generateAccessToken_withoutRoles_extractRolesReturnsEmptyList() {
        String token = jwtUtil.generateAccessToken("elder.demo@eldersphere.app", "90005");

        assertThat(jwtUtil.extractRoles(token)).isEmpty();
    }

    @Test
    void validateToken_true_forMatchingSubjectAndUnexpired() {
        String token = jwtUtil.generateAccessToken("caretaker.demo@eldersphere.app", "90003");

        assertThat(jwtUtil.validateToken(token, "caretaker.demo@eldersphere.app")).isTrue();
    }

    @Test
    void validateToken_false_forMismatchedEmail() {
        String token = jwtUtil.generateAccessToken("caretaker.demo@eldersphere.app", "90003");

        assertThat(jwtUtil.validateToken(token, "someone.else@eldersphere.app")).isFalse();
    }

    @Test
    void validateToken_false_forExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);
        String token = jwtUtil.generateAccessToken("caretaker.demo@eldersphere.app", "90003");

        assertThat(jwtUtil.validateToken(token, "caretaker.demo@eldersphere.app")).isFalse();
    }

    @Test
    void validateToken_false_forGarbageToken() {
        assertThat(jwtUtil.validateToken("not-a-real-jwt", "caretaker.demo@eldersphere.app")).isFalse();
    }

    @Test
    void extractRoles_returnsEmptyList_forGarbageToken() {
        assertThat(jwtUtil.extractRoles("not-a-real-jwt")).isEmpty();
    }
}
