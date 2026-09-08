package com.eldersphere.services;

import com.eldersphere.dtos.Role.RoleRequestDTO;
import com.eldersphere.dtos.Role.RoleResponseDTO;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface RoleService {
    RoleResponseDTO create(RoleRequestDTO request) throws GenericException;
    RoleResponseDTO update(Long id, RoleRequestDTO request) throws GenericException;
    RoleResponseDTO getById(Long id) throws GenericException;
    List<RoleResponseDTO> getAll();
    void delete(Long id) throws GenericException;
}
