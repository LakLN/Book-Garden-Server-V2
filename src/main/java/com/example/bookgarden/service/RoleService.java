package com.example.bookgarden.service;

import com.example.bookgarden.entity.Role;
import com.example.bookgarden.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

}
