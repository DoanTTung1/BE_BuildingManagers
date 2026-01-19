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

    // 1. Lấy danh sách User (Hỗ trợ lọc theo trạng thái)
    // Frontend gửi: /api/users?status=0 (Lấy thùng rác) hoặc /api/users (Lấy
    // active)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(userService.getUsersByStatus(status));
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

    // 4. Xóa mềm (Đưa vào thùng rác)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Đã chuyển vào thùng rác!");
    }

    // 5. 👇 MỚI: Xóa vĩnh viễn (Hard Delete)
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<String> hardDeleteUser(@PathVariable Long id) {
        userService.hardDeleteUser(id);
        return ResponseEntity.ok("Đã xóa vĩnh viễn!");
    }

    // 6. 👇 MỚI: Khôi phục (Restore)
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok("Khôi phục thành công!");
    }
}