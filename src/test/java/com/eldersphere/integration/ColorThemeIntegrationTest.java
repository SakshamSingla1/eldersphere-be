package com.eldersphere.integration;

import com.eldersphere.dtos.Authentication.AuthLoginDTO;
import com.eldersphere.dtos.Authentication.AuthRegisterDTO;
import com.eldersphere.dtos.Authentication.LoginResponseDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeRequestDTO;
import com.eldersphere.dtos.ColorTheme.ColorThemeResponseDTO;
import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.dtos.ColorTheme.UserThemeUpdateRequest;
import com.eldersphere.entities.BackgroundColors;
import com.eldersphere.entities.ColorGroup;
import com.eldersphere.entities.ColorPalette;
import com.eldersphere.entities.TextColors;
import com.eldersphere.enums.ColorThemeStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end color theme catalog + per-user active-theme selection, against the real
 * (Testcontainers) Postgres with the V40-seeded 6 presets: SUPER_ADMIN-only enforcement on
 * catalog CRUD (superadmin@eldersphere.app vs. the plain admin@eldersphere.app demo account),
 * and a freshly-registered user picking/clearing their own active theme.
 */
class ColorThemeIntegrationTest extends AbstractIntegrationTest {

    private static final String SUPER_ADMIN_EMAIL = "superadmin@eldersphere.app";
    private static final String PLAIN_ADMIN_EMAIL = "admin@eldersphere.app";
    private static final String DEMO_PASSWORD = "Password123!";

    @Test
    void list_returnsSixSeededPresets_withExactlyOneDefault() {
        String token = login(SUPER_ADMIN_EMAIL, DEMO_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/color-themes"), HttpMethod.GET, authEntity(token), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ColorThemeResponseDTO> themes = extractDataList(response);
        assertThat(themes).hasSizeGreaterThanOrEqualTo(6);
        assertThat(themes).filteredOn(ColorThemeResponseDTO::isDefault).hasSize(1);
        assertThat(themes).filteredOn(ColorThemeResponseDTO::isDefault).extracting(ColorThemeResponseDTO::getName)
                .containsExactly("Forest & Terracotta");
        assertThat(themes).extracting(ColorThemeResponseDTO::getName)
                .contains("Forest & Terracotta", "Ocean Calm", "Warm Sunset", "Lavender Care", "Slate Professional", "Rosewood Bloom");
        assertThat(themes).allSatisfy(t -> assertThat(t.getStatus()).isEqualTo(ColorThemeStatusEnum.ACTIVE));
    }

    @Test
    void themeCrud_isSuperAdminOnly_plainAdminForbidden() {
        String superAdminToken = login(SUPER_ADMIN_EMAIL, DEMO_PASSWORD);
        String plainAdminToken = login(PLAIN_ADMIN_EMAIL, DEMO_PASSWORD);

        ColorThemeRequestDTO createRequest = ColorThemeRequestDTO.builder()
                .name("Test Theme " + System.nanoTime())
                .palette(samplePalette())
                .isDefault(false)
                .status(ColorThemeStatusEnum.ACTIVE)
                .build();

        // A plain ADMIN (not SUPER_ADMIN) is forbidden from every write operation.
        ResponseEntity<String> forbiddenCreate = restTemplate.exchange(
                baseUrl("/api/v1/color-themes"), HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(plainAdminToken)), String.class);
        assertThat(forbiddenCreate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // SUPER_ADMIN can create.
        ResponseEntity<String> createResponse = restTemplate.exchange(
                baseUrl("/api/v1/color-themes"), HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(superAdminToken)), String.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ColorThemeResponseDTO created = extractData(createResponse, ColorThemeResponseDTO.class);
        assertThat(created.getId()).isNotNull();

        try {
            // Plain ADMIN forbidden from update/delete too.
            ColorThemeRequestDTO updateRequest = ColorThemeRequestDTO.builder()
                    .name(created.getName() + " Updated")
                    .palette(samplePalette())
                    .isDefault(false)
                    .status(ColorThemeStatusEnum.ACTIVE)
                    .build();
            ResponseEntity<String> forbiddenUpdate = restTemplate.exchange(
                    baseUrl("/api/v1/color-themes/" + created.getId()), HttpMethod.PUT,
                    new HttpEntity<>(updateRequest, authHeaders(plainAdminToken)), String.class);
            assertThat(forbiddenUpdate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            ResponseEntity<String> forbiddenDelete = restTemplate.exchange(
                    baseUrl("/api/v1/color-themes/" + created.getId()), HttpMethod.DELETE,
                    authEntity(plainAdminToken), String.class);
            assertThat(forbiddenDelete.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

            // SUPER_ADMIN can update.
            ResponseEntity<String> updateResponse = restTemplate.exchange(
                    baseUrl("/api/v1/color-themes/" + created.getId()), HttpMethod.PUT,
                    new HttpEntity<>(updateRequest, authHeaders(superAdminToken)), String.class);
            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(extractData(updateResponse, ColorThemeResponseDTO.class).getName()).isEqualTo(created.getName() + " Updated");
        } finally {
            // SUPER_ADMIN can delete - clean up so this test doesn't leak state into others.
            ResponseEntity<String> deleteResponse = restTemplate.exchange(
                    baseUrl("/api/v1/color-themes/" + created.getId()), HttpMethod.DELETE,
                    authEntity(superAdminToken), String.class);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deletingTheDefaultTheme_isRejected() {
        String superAdminToken = login(SUPER_ADMIN_EMAIL, DEMO_PASSWORD);

        List<ColorThemeResponseDTO> themes = extractDataList(restTemplate.exchange(
                baseUrl("/api/v1/color-themes"), HttpMethod.GET, authEntity(superAdminToken), String.class));
        ColorThemeResponseDTO defaultTheme = themes.stream().filter(ColorThemeResponseDTO::isDefault).findFirst().orElseThrow();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/color-themes/" + defaultTheme.getId()), HttpMethod.DELETE,
                authEntity(superAdminToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void userTheme_defaultsWhenUnset_thenSetThenClear_fallsBackToDefaultAgain() {
        // A brand-new user so this test doesn't share mutable state with any other test.
        String uniqueEmail = "theme-flow-" + System.nanoTime() + "@eldersphere.test";
        registerFamilyMember(uniqueEmail);
        // Single login for the whole test: reused both for its own inline-theme assertion and
        // as the bearer token for every subsequent call below (also keeps this test's auth-POST
        // count to the bare minimum against the shared per-IP rate limit bucket).
        LoginResponseDTO loginData = loginFull(uniqueEmail, "Password123!");
        String token = loginData.getToken();

        // 1. Freshly-registered user has never picked a theme -> login's inline activeTheme
        // (avoids a second round-trip) resolves to the default.
        assertThat(loginData.getActiveTheme().isUsingDefault()).isTrue();
        assertThat(loginData.getActiveTheme().getThemeName()).isEqualTo("Forest & Terracotta");
        assertThat(loginData.getActiveTheme().getPalette()).isNotNull();
        assertThat(loginData.getActiveTheme().getPalette().getPrimary().getMain()).isEqualTo("#2F6F5E");

        // 2. GET /users/me/theme agrees.
        UserThemeResponseDTO initial = extractData(restTemplate.exchange(
                baseUrl("/api/v1/users/me/theme"), HttpMethod.GET, authEntity(token), String.class), UserThemeResponseDTO.class);
        assertThat(initial.isUsingDefault()).isTrue();
        assertThat(initial.getThemeName()).isEqualTo("Forest & Terracotta");

        // 3. Pick "Ocean Calm" explicitly.
        List<ColorThemeResponseDTO> themes = extractDataList(restTemplate.exchange(
                baseUrl("/api/v1/color-themes"), HttpMethod.GET, authEntity(token), String.class));
        ColorThemeResponseDTO oceanCalm = themes.stream().filter(t -> t.getName().equals("Ocean Calm")).findFirst().orElseThrow();

        ResponseEntity<String> setResponse = restTemplate.exchange(
                baseUrl("/api/v1/users/me/theme"), HttpMethod.PUT,
                new HttpEntity<>(UserThemeUpdateRequest.builder().themeId(oceanCalm.getId()).build(), authHeaders(token)),
                String.class);
        assertThat(setResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserThemeResponseDTO afterSet = extractData(setResponse, UserThemeResponseDTO.class);
        assertThat(afterSet.isUsingDefault()).isFalse();
        assertThat(afterSet.getThemeName()).isEqualTo("Ocean Calm");
        assertThat(afterSet.getPalette().getPrimary().getMain()).isEqualTo("#1F6F8B");

        // 4. GET agrees.
        UserThemeResponseDTO afterSetGet = extractData(restTemplate.exchange(
                baseUrl("/api/v1/users/me/theme"), HttpMethod.GET, authEntity(token), String.class), UserThemeResponseDTO.class);
        assertThat(afterSetGet.getThemeName()).isEqualTo("Ocean Calm");
        assertThat(afterSetGet.isUsingDefault()).isFalse();

        // 5. Clear (null) -> falls back to the default again.
        ResponseEntity<String> clearResponse = restTemplate.exchange(
                baseUrl("/api/v1/users/me/theme"), HttpMethod.PUT,
                new HttpEntity<>(UserThemeUpdateRequest.builder().themeId(null).build(), authHeaders(token)),
                String.class);
        assertThat(clearResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserThemeResponseDTO afterClear = extractData(clearResponse, UserThemeResponseDTO.class);
        assertThat(afterClear.isUsingDefault()).isTrue();
        assertThat(afterClear.getThemeName()).isEqualTo("Forest & Terracotta");
    }

    @Test
    void settingAnUnknownThemeId_isRejected() {
        String uniqueEmail = "theme-unknown-" + System.nanoTime() + "@eldersphere.test";
        registerFamilyMember(uniqueEmail);
        String token = login(uniqueEmail, "Password123!");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/users/me/theme"), HttpMethod.PUT,
                new HttpEntity<>(UserThemeUpdateRequest.builder().themeId(999999L).build(), authHeaders(token)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ColorPalette samplePalette() {
        return ColorPalette.builder()
                .primary(ColorGroup.builder().main("#123456").light("#345678").dark("#012345").contrastText("#FFFFFF").build())
                .secondary(ColorGroup.builder().main("#654321").light("#765432").dark("#543210").contrastText("#FFFFFF").build())
                .background(BackgroundColors.builder().defaultBg("#F0F0F0").paper("#FFFFFF").build())
                .text(TextColors.builder().primary("#111111").secondary("#444444").build())
                .build();
    }

    // RateLimitingFilter buckets auth POSTs per client IP (10/min) and treats 127.0.0.1 as a
    // trusted proxy that honors X-Forwarded-For - all of TestRestTemplate's calls land on
    // 127.0.0.1, and the rate limiter bucket is a singleton bean in the Spring context shared
    // across every integration test class in this JVM run (Testcontainers Postgres + context
    // caching), so this test class's several logins would otherwise burn through the SAME
    // budget as AuthFlowIntegrationTest/AdminRbacIntegrationTest/MultiRoleLoginIntegrationTest
    // and intermittently 429 them. Tagging every auth call here with its own fake
    // X-Forwarded-For gives this class an isolated bucket instead.
    private static final String TEST_CLIENT_IP = "10.77.77.77";

    private void registerFamilyMember(String email) {
        AuthRegisterDTO registerRequest = AuthRegisterDTO.builder()
                .fullName("Theme Flow Test User")
                .email(email)
                .phone("+15550002222")
                .password("Password123!")
                .userType(UserTypeEnum.FAMILY_MEMBER)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", TEST_CLIENT_IP);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/auth/register"), HttpMethod.POST, new HttpEntity<>(registerRequest, headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private String login(String email, String password) {
        return loginFull(email, password).getToken();
    }

    private LoginResponseDTO loginFull(String email, String password) {
        AuthLoginDTO loginRequest = AuthLoginDTO.builder().email(email).password(password).build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", TEST_CLIENT_IP);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/auth/login"), HttpMethod.POST, new HttpEntity<>(loginRequest, headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return extractData(response, LoginResponseDTO.class);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpEntity<Void> authEntity(String token) {
        return new HttpEntity<>(authHeaders(token));
    }

    private List<ColorThemeResponseDTO> extractDataList(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            return objectMapper.readerForListOf(ColorThemeResponseDTO.class).readValue(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract 'data' list from response body: " + response.getBody(), e);
        }
    }
}
