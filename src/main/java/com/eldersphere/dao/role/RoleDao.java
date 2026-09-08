package com.eldersphere.dao.role;

import com.eldersphere.dao.IDao;
import com.eldersphere.entities.Role;
import com.eldersphere.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleDao implements IDao<Role, Long> {

    private final RoleRepository roleRepository;

    @Override
    public JpaRepository<Role, Long> getRepository() {
        return roleRepository;
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }
}
