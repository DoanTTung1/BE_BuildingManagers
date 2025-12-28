package com.example.buildingmanager.services.auth;
import com.example.buildingmanager.entities.User;
import com.example.buildingmanager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        // 1. Tìm user theo username và status = 1 (Active)
        User user = userRepository.findByUserNameAndStatus(userName, 1)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive with username: " + userName));

        // 2. Map từ Entity Role sang Authority của Spring Security
        // Lưu ý: user.getRoles() bây giờ trả về Set<Role> trực tiếp (do file User.java mới)
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode().toUpperCase())) // VD: ROLE_STAFF
                .collect(Collectors.toList());

        // 3. Trả về đối tượng User của Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getPassword(),
                authorities
        );
    }
}