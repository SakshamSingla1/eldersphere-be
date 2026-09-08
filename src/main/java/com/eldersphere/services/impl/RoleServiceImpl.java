package com.eldersphere.services.impl;

import com.eldersphere.dao.role.RoleDao;
import com.eldersphere.dtos.Role.RoleRequestDTO;
import com.eldersphere.dtos.Role.RoleResponseDTO;
import com.eldersphere.entities.Role;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.RoleStatusEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;

    @Override
    @Transactional
    public RoleResponseDTO create(RoleRequestDTO request) throws GenericException {
        if (roleDao.existsByName(request.getName())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_ROLE, "A role with this name already exists");
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : RoleStatusEnum.ACTIVE)
                .build();
        return toResponse(roleDao.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDTO update(Long id, RoleRequestDTO request) throws GenericException {
        Role role = roleDao.findById(id, true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        if (request.getStatus() != null) role.setStatus(request.getStatus());
        return toResponse(roleDao.save(role));
    }

    @Override
    public RoleResponseDTO getById(Long id) throws GenericException {
        Role role = roleDao.findById(id, true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        return toResponse(role);
    }

    @Override
    public List<RoleResponseDTO> getAll() {
        return roleDao.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        Role role = roleDao.findById(id, true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        roleDao.deleteById(id);
    }

    private RoleResponseDTO toResponse(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setStatus(role.getStatus());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
}
