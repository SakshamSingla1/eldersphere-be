package com.eldersphere.dao.role;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.RolePermission;
import com.eldersphere.repositories.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionDao implements IDao<RolePermission, Long> {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public JpaRepository<RolePermission, Long> getRepository() {
        return rolePermissionRepository;
    }

    public RolePermission save(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    public List<RolePermission> findByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    public void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }
}
