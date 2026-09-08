package com.eldersphere.services;

import com.eldersphere.dtos.Authentication.*;
import com.eldersphere.exceptions.GenericException;

public interface AuthService {

    AuthResponseDTO register(AuthRegisterDTO registerDTO) throws GenericException;

    LoginResponseDTO login(AuthLoginDTO loginDTO) throws GenericException;

    String forgotPassword(PasswordResetRequestDTO requestDTO) throws GenericException;

    String validatePasswordResetToken(String token) throws GenericException;

    String resetPassword(PasswordResetConfirmDTO dto) throws GenericException;

    String changePassword(String authorizationHeader, ChangePasswordDTO dto) throws GenericException;
}
