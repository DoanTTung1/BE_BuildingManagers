package com.example.buildingmanager.mapper;

import com.example.buildingmanager.entities.Role;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.models.user.UserDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    public UserDTO toUserDTO(User entity) {
        if (entity == null)
            return null;

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());

        // Entity của bạn là userName -> DTO là username
        dto.setUsername(entity.getUserName());

        dto.setFullName(entity.getFullName());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setStatus(entity.getStatus());

        // Mapping Avatar
        dto.setAvatar(entity.getAvatar());

        // Mapping OTP Status
        dto.setPhoneVerified(entity.isPhoneVerified());

        // Mapping Roles
        if (entity.getRoles() != null) {
            List<String> roleCodes = entity.getRoles().stream()
                    .map(Role::getCode) // Lấy code (ADMIN, USER)
                    .collect(Collectors.toList());
            dto.setRoles(roleCodes);
        }

        return dto;
    }

    public User toUserEntity(UserDTO dto) {
        User entity = new User();
        entity.setUserName(dto.getUsername());
        entity.setFullName(dto.getFullName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(1);
        return entity;
    }
}