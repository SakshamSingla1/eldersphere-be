package com.eldersphere.services;

import com.eldersphere.dtos.Admin.AdminCreateUserRequest;
import com.eldersphere.dtos.User.UserResponse;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    UserResponse createUser(AdminCreateUserRequest dto) throws GenericException;

    Page<UserResponse> listUsers(String search, UserTypeEnum userType, UserStatusEnum status, Pageable pageable);

    UserResponse getUser(Long id) throws GenericException;

    UserResponse updateStatus(Long id, UserStatusEnum status) throws GenericException;

    int bulkUpdateStatus(List<Long> userIds, UserStatusEnum status) throws GenericException;

    UserResponse assignRole(Long id, Long roleId) throws GenericException;

    void deleteUser(Long id) throws GenericException;
}
