package com.example.buildingmanager.controllers;

import com.example.buildingmanager.entities.Contact;
import com.example.buildingmanager.models.contact.ContactDTO;
import com.example.buildingmanager.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Cho phép Frontend (React) gọi API
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    // ==================== USER ====================

    // 1. Gửi liên hệ mới (User dùng)
    // POST: /api/contacts
    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody ContactDTO contactDTO) {
        try {
            // Chuyển đổi từ DTO sang Entity
            Contact contact = new Contact();
            contact.setName(contactDTO.getName());
            contact.setEmail(contactDTO.getEmail());
            contact.setPhone(contactDTO.getPhone());
            contact.setSubject(contactDTO.getSubject());
            contact.setMessage(contactDTO.getMessage());

            // Set mặc định
            contact.setStatus("UNREAD"); // Trạng thái mặc định là chưa đọc

            // Lưu vào Database
            contactRepository.save(contact);

            return ResponseEntity.ok("Gửi liên hệ thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ==================== ADMIN ====================

    // 2. Lấy danh sách tất cả liên hệ (Admin dùng)
    // GET: /api/contacts
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        // Lấy tất cả và sắp xếp ngày tạo mới nhất lên đầu (DESC)
        List<Contact> contacts = contactRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(contacts);
    }

    // 3. Xóa liên hệ (Admin dùng)
    // DELETE: /api/contacts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        if (!contactRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        contactRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa liên hệ thành công!");
    }

    // 4. Cập nhật trạng thái đã xử lý (Admin dùng)
    // PUT: /api/contacts/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> markAsProcessed(@PathVariable Long id) {
        return contactRepository.findById(id).map(contact -> {
            contact.setStatus("PROCESSED"); // Chuyển trạng thái sang Đã Xử Lý
            contactRepository.save(contact);
            return ResponseEntity.ok("Đã cập nhật trạng thái thành công!");
        }).orElse(ResponseEntity.notFound().build());
    }
}