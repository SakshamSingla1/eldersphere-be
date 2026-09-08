package com.eldersphere.services.impl;

import com.eldersphere.dao.authentication.PasswordResetTokenDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dtos.Authentication.*;
import com.eldersphere.entities.PasswordResetToken;
import com.eldersphere.entities.Role;
import com.eldersphere.entities.User;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.repositories.RoleRepository;
import com.eldersphere.security.JwtUtil;
import com.eldersphere.services.AuthService;
import com.eldersphere.services.EmailService;
import com.eldersphere.services.UserRoleService;
import com.eldersphere.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Self-registration is only open to ELDER, CARETAKER and FAMILY_MEMBER.
    // ADMIN/SUPER_ADMIN accounts can only be created via the admin API by an
    // existing SUPER_ADMIN (see AdminServiceImpl).
    private static final Set<UserTypeEnum> SELF_REGISTERABLE_TYPES =
            EnumSet.of(UserTypeEnum.ELDER, UserTypeEnum.CARETAKER, UserTypeEnum.FAMILY_MEMBER);

    private final UserDao userDao;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final Helper helper;
    private final UserRoleService userRoleService;

    @Value("${app.password-reset.token-expiry-minutes}")
    private int passwordResetExpiryMinutes;

    @Value("${app.password-reset.url}")
    private String passwordResetUrl;

    @Override
    @Transactional
    public AuthResponseDTO register(AuthRegisterDTO registerDTO) throws GenericException {
        if (userDao.existsByEmail(registerDTO.getEmail())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_EMAIL, "An account with this email already exists");
        }

        if (!SELF_REGISTERABLE_TYPES.contains(registerDTO.getUserType())) {
            throw new GenericException(ExceptionCodeEnum.VALIDATION_FAILED,
                    "Self-registration is only allowed for ELDER, CARETAKER, or FAMILY_MEMBER user types");
        }

        // No OTP/email-verification step in this MVP (see README "Scope decisions"),
        // so a self-registered account is active immediately.
        User user = User.builder()
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .fullName(registerDTO.getFullName())
                .passwordHash(passwordEncoder.encode(registerDTO.getPassword()))
                .userType(registerDTO.getUserType())
                .status(UserStatusEnum.ACTIVE)
                .build();
        user = userDao.save(user);
        // Self-registration always grants exactly the one role the user registered as, so this
        // mapping row has no "granted by" admin — it's self-granted at signup.
        userRoleService.ensurePrimaryMapping(user, null);

        return AuthResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .build();
    }

    @Override
    public LoginResponseDTO login(AuthLoginDTO loginDTO) throws GenericException {
        User user = userDao.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.INVALID_CREDENTIALS, "Invalid email or password"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new GenericException(ExceptionCodeEnum.INVALID_CREDENTIALS, "Invalid email or password");
        }

        if (user.getStatus() == UserStatusEnum.INACTIVE) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN, "This account has been deactivated");
        }

        String roleName = user.getUserType() != null ? user.getUserType().name() : null;
        if (user.getRoleId() != null) {
            Role role = roleRepository.findById(user.getRoleId()).orElse(null);
            if (role != null) roleName = role.getName();
        }

        List<UserTypeEnum> roleTypes = userRoleService.getRoleTypes(user.getId());
        List<String> roleTypeNames = roleTypes.stream().map(UserTypeEnum::name).toList();
        String primaryRoleName = user.getUserType() != null ? user.getUserType().name() : null;
        String token = jwtUtil.generateAccessToken(user.getEmail(), String.valueOf(user.getId()), primaryRoleName, roleTypeNames);

        return LoginResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .status(user.getStatus())
                .roleId(user.getRoleId())
                .roleName(roleName)
                .roles(roleTypes)
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public String forgotPassword(PasswordResetRequestDTO requestDTO) throws GenericException {
        User user = userDao.findByEmail(requestDTO.getEmail()).orElse(null);
        // Do not reveal whether the email exists — always return a generic success message.
        if (user != null) {
            passwordResetTokenDao.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .userId(user.getId())
                    .expiryDate(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes))
                    .used(false)
                    .build();
            passwordResetTokenDao.save(resetToken);

            String resetLink = passwordResetUrl + "?token=" + token;
            String html = "<p>Hello " + user.getFullName() + ",</p>"
                    + "<p>We received a request to reset your ElderSphere password. This link expires in "
                    + passwordResetExpiryMinutes + " minutes:</p>"
                    + "<p><a href=\"" + resetLink + "\">Reset your password</a></p>"
                    + "<p>If you did not request this, you can safely ignore this email.</p>";
            emailService.sendEmail(user.getEmail(), "Reset your ElderSphere password", html);
        }
        return "If an account with that email exists, a password reset link has been sent.";
    }

    @Override
    public String validatePasswordResetToken(String token) throws GenericException {
        PasswordResetToken resetToken = passwordResetTokenDao.findByToken(token)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.PASSWORD_RESET_FAILED, "Invalid or expired token"));
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new GenericException(ExceptionCodeEnum.PASSWORD_RESET_FAILED, "Invalid or expired token");
        }
        return "Token is valid";
    }

    @Override
    @Transactional
    public String resetPassword(PasswordResetConfirmDTO dto) throws GenericException {
        PasswordResetToken resetToken = passwordResetTokenDao.findByToken(dto.getToken())
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.PASSWORD_RESET_FAILED, "Invalid or expired token"));
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new GenericException(ExceptionCodeEnum.PASSWORD_RESET_FAILED, "Invalid or expired token");
        }

        User user = userDao.findById(resetToken.getUserId(), true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userDao.save(user);

        resetToken.setUsed(true);
        passwordResetTokenDao.save(resetToken);

        return "Password reset successfully";
    }

    @Override
    @Transactional
    public String changePassword(String authorizationHeader, ChangePasswordDTO dto) throws GenericException {
        User user = helper.getUserFromHeader(authorizationHeader);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new GenericException(ExceptionCodeEnum.PASSWORD_MISMATCH, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userDao.save(user);
        return "Password changed successfully";
    }
}
