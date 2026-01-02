package com.example.buildingmanager.models.user;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;

    // Đổi userName -> username để khớp với JSON chuẩn Frontend gửi lên/nhận về
    private String username;

    private String fullName;
    private String phone;
    private String email;
    private Integer status;

    // Lưu ý: Password thường không nên trả về Frontend (trừ khi cần thiết),
    // nhưng cứ để đây nếu bạn dùng chung DTO cho việc Tạo mới User.
    private String password;

    // --- CÁC TRƯỜNG MỚI BỔ SUNG (BẮT BUỘC) ---

    private String avatar; // Chứa link ảnh Cloudinary
    private boolean phoneVerified; // Trạng thái đã xác thực OTP chưa

    // Đổi tên roleCodes -> roles để khớp với UserConverter.java
    private List<String> roles = new ArrayList<>();
}