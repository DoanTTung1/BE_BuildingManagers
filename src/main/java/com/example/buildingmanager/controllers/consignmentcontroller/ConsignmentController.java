package com.example.buildingmanager.controllers.consignmentcontroller;

import com.example.buildingmanager.enums.ConsignmentStatus;
import com.example.buildingmanager.models.user.ConsignmentDTO;
import com.example.buildingmanager.services.consignmentservice.ConsignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consignments")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép React gọi API
public class ConsignmentController {

    private final ConsignmentService consignmentService;

    @PostMapping
    public ResponseEntity<?> sendConsignment(@Valid @RequestBody ConsignmentDTO consignmentDTO) {
        // @Valid sẽ tự động check các điều kiện trong DTO (NotBlank, Min, Max...)
        // Nếu lỗi nó sẽ ném ra Exception (cần GlobalExceptionHandler để bắt đẹp hơn)

        consignmentService.createConsignment(consignmentDTO);

        return ResponseEntity.ok("Gửi yêu cầu ký gửi thành công! Nhân viên sẽ liên hệ sớm.");
    }

    // 1. Lấy danh sách ký gửi
    // GET /api/consignments?page=0&size=10&status=PENDING
    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // Sau này nhớ bật cái này lên
    public ResponseEntity<?> getList(
            @RequestParam(required = false) ConsignmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return ResponseEntity.ok(consignmentService.getConsignments(status, pageable));
    }

    // 2. Cập nhật trạng thái
    // PUT /api/consignments/1/status?status=CONTACTED
    @PutMapping("/{id}/status")
    // @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam ConsignmentStatus status) {
        consignmentService.updateStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }

    // 3. Xóa
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        consignmentService.deleteConsignment(id);
        return ResponseEntity.ok("Đã xóa yêu cầu.");
    }
}