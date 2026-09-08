package com.eldersphere.repositories;

import com.eldersphere.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
