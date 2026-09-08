package com.eldersphere.controllers;

import com.eldersphere.dao.authentication.RefreshTokenDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Authentication.*;
import com.eldersphere.entities.RefreshToken;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.security.JwtUtil;
import com.eldersphere.services.AuthService;
import com.eldersphere.services.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Registration, login, JWT + refresh token, and password reset")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenDao refreshTokenDao;
    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    private final UserRoleService userRoleService;

    @Value("${jwt.refresh-token.expiration-days}")
    private int refreshTokenExpirationDays;

    @Operation(summary = "Register a new user", description = "Registers a new user account (elder, family, caretaker). No email verification step in this MVP — the account is active immediately.")
    @PostMapping("/register")
    public ResponseEntity<ResponseModel<AuthResponseDTO>> register(@Valid @RequestBody AuthRegisterDTO registerDTO) throws GenericException {
        AuthResponseDTO response = authService.register(registerDTO);
        return ApiResponse.createSuccess(response, "User registered successfully");
    }

    @Operation(summary = "Login", description = "Authenticates a user by email and password. Returns a JWT access token and sets httpOnly access/refresh cookies.")
    @PostMapping("/login")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> login(@Valid @RequestBody AuthLoginDTO loginDTO,
                                                                  HttpServletResponse httpResponse) throws GenericException {
        LoginResponseDTO response = authService.login(loginDTO);
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenDao.save(RefreshToken.builder()
                .userId(response.getId())
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .build());
        setAuthCookies(httpResponse, response.getToken(), refreshTokenValue);
        return ApiResponse.successResponse(response, "Login successful");
    }

    @Operation(summary = "Refresh access token", description = "Issues a new access token using the httpOnly refresh token cookie.")
    @PostMapping("/refresh")
    public ResponseEntity<ResponseModel<Void>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = extractCookieValue(request, "refreshToken");
        if (refreshTokenValue == null) {
            return ApiResponse.failureResponse(null, "No refresh token provided");
        }
        RefreshToken storedToken = refreshTokenDao.findByToken(refreshTokenValue).orElse(null);
        if (storedToken == null || storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.failureResponse(null, "Invalid or expired refresh token");
        }
        User user = userDao.findById(storedToken.getUserId(), true);
        if (user == null) {
            return ApiResponse.failureResponse(null, "User not found");
        }
        List<UserTypeEnum> roleTypes = userRoleService.getRoleTypes(user.getId());
        List<String> roleTypeNames = roleTypes.stream().map(UserTypeEnum::name).toList();
        String primaryRoleName = user.getUserType() != null ? user.getUserType().name() : null;
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), String.valueOf(user.getId()), primaryRoleName, roleTypeNames);
        refreshTokenDao.deleteByToken(refreshTokenValue);
        String newRefreshToken = UUID.randomUUID().toString();
        refreshTokenDao.save(RefreshToken.builder()
                .userId(storedToken.getUserId())
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .build());
        setAuthCookies(response, newAccessToken, newRefreshToken);
        return ApiResponse.successResponse();
    }

    @Operation(summary = "Logout", description = "Clears auth cookies and revokes the refresh token.")
    @PostMapping("/logout")
    public ResponseEntity<ResponseModel<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = extractCookieValue(request, "refreshToken");
        if (refreshTokenValue != null) {
            refreshTokenDao.deleteByToken(refreshTokenValue);
        }
        clearAuthCookies(response);
        return ApiResponse.successResponse();
    }

    @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email if the account exists.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseModel<String>> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO requestDTO) throws GenericException {
        String message = authService.forgotPassword(requestDTO);
        return ApiResponse.successResponse(message, message);
    }

    @Operation(summary = "Validate password reset token", description = "Checks whether a password reset token is still valid before showing the reset form.")
    @GetMapping("/validate-reset-token")
    public ResponseEntity<ResponseModel<String>> validateResetToken(@RequestParam String token) throws GenericException {
        String message = authService.validatePasswordResetToken(token);
        return ApiResponse.successResponse(message, "Token is valid");
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using the token received via email.")
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseModel<String>> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO requestDTO) throws GenericException {
        String message = authService.resetPassword(requestDTO);
        return ApiResponse.successResponse(message, "Password reset successfully");
    }

    @Operation(summary = "Change password", description = "Changes the password for the currently authenticated user.")
    @PutMapping("/change-password")
    public ResponseEntity<ResponseModel<String>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ChangePasswordDTO requestDTO) throws GenericException {
        String message = authService.changePassword(authorizationHeader, requestDTO);
        return ApiResponse.successResponse(message, "Password changed successfully");
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).secure(true).path("/").maxAge(36000).sameSite("Strict").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(true).path("/api/v1/auth/refresh").maxAge(refreshTokenExpirationDays * 24 * 3600).sameSite("Strict").build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).path("/").maxAge(0).sameSite("Strict").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/api/v1/auth/refresh").maxAge(0).sameSite("Strict").build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
