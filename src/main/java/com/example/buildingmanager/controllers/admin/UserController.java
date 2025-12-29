package com.example.buildingmanager.controllers.admin;

import com.example.buildingmanager.models.user.UserDTO;
import com.example.buildingmanager.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được quản lý User
public class UserController {

    private final IUserService userService;

    // 1. Lấy danh sách tất cả User (để hiện bảng quản lý)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 2. Chỉ lấy danh sách STAFF (để hiện trong dropdown giao việc)
    @GetMapping("/staffs")
    public ResponseEntity<List<UserDTO>> getStaffs() {
        return ResponseEntity.ok(userService.getAllStaffs());
    }

    // 3. Tạo nhân viên mới
    @PostMapping
    public ResponseEntity<UserDTO> createStaff(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createStaff(userDTO));
    }

    // 4. Xóa nhân viên
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa thành công!");
    }
}