package com.example.buildingmanager.entities;

import jakarta.persistence.*; 
import java.util.Date;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "subject")
    private String subject; // Ví dụ: "Mua", "Thuê", "Ký gửi"...

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // Dùng TEXT để lưu được nội dung dài

    @Column(name = "status")
    private String status = "UNREAD"; // Trạng thái: UNREAD (Chưa đọc), PROCESSED (Đã xử lý)

    @Column(name = "created_date")
    private Date createdDate;

    // Tự động lưu ngày giờ hiện tại khi tạo mới
    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date();
    }

    // --- GETTER & SETTER ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}