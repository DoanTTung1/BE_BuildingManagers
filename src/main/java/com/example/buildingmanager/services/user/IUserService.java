package com.example.buildingmanager.services.user;

import com.example.buildingmanager.models.user.UserDTO;
import java.util.List;

public interface IUserService {
    List<UserDTO> getAllUsers();           // Lấy tất cả user
    List<UserDTO> getAllStaffs();          // Chỉ lấy nhân viên (để hiện checkbox giao việc)
    UserDTO createStaff(UserDTO userDTO);  // Tạo nhân viên mới
    void deleteUser(Long id);              // Xóa user
}