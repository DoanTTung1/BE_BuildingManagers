package com.example.buildingmanager.enums;

public enum ConsignmentStatus {
    PENDING,            // Chờ xử lý (Vừa gửi xong)
    CONTACTED,          // Sale đã gọi điện xác nhận
    SURVEYED,           // Đã đi khảo sát thực tế
    APPROVED,           // Đã duyệt (Chuyển thành Building chính thức)
    REJECTED,           // Từ chối (Spam, thông tin sai)
    CANCELLED           // Khách hàng hủy yêu cầu
}