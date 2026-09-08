package com.eldersphere.dao.permission;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Permission;
import com.eldersphere.repositories.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PermissionDao implements IDao<Permission, Long> {

    private final PermissionRepository permissionRepository;

    @Override
    public JpaRepository<Permission, Long> getRepository() {
        return permissionRepository;
    }

    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }
}
