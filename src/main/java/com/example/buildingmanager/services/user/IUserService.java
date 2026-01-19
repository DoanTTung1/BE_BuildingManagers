package com.example.buildingmanager.services.user;

import com.example.buildingmanager.models.user.UserDTO;
import com.example.buildingmanager.models.user.UserUpdateRequest;

import java.util.List;

public interface IUserService {

    // 👇 1. SỬA: Thay hàm getAllUsers cũ bằng hàm này để hỗ trợ lọc Active/Thùng
    // rác
    List<UserDTO> getUsersByStatus(Integer status);

    List<UserDTO> getAllStaffs(); // Lấy nhân viên (để giao việc)

    UserDTO createStaff(UserDTO userDTO); // Tạo mới

    void deleteUser(Long id); // Xóa mềm (Soft Delete)

    // 👇 2. MỚI: Xóa vĩnh viễn (Hard Delete)
    void hardDeleteUser(Long id);

    // 👇 3. MỚI: Khôi phục (Restore từ thùng rác)
    void restoreUser(Long id);

    UserDTO updateProfile(String currentUsername, UserUpdateRequest request); // Cập nhật Profile
}