package com.example.buildingmanager.services.consignmentservice;

import com.example.buildingmanager.models.user.ConsignmentDTO;
import com.example.buildingmanager.entities.ConsignmentRequest;
import com.example.buildingmanager.enums.ConsignmentStatus;
import com.example.buildingmanager.repositories.ConsignmentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsignmentService {

    private final ConsignmentRepository consignmentRepository;

    @Transactional
    public ConsignmentRequest createConsignment(ConsignmentDTO dto) {
        // 1. Map từ DTO sang Entity (Có thể dùng ModelMapper, ở đây code tay cho rõ)
        ConsignmentRequest request = ConsignmentRequest.builder()
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerEmail(dto.getCustomerEmail())
                .buildingName(dto.getBuildingName())
                .address(dto.getAddress())
                .districtCode(dto.getDistrictCode())
                .ward(dto.getWard())
                .direction(dto.getDirection())
                .transactionType(dto.getTransactionType())
                .numberOfFloors(dto.getNumberOfFloors())
                .floorArea(dto.getFloorArea())
                .totalArea(dto.getTotalArea())
                .expectedPrice(dto.getExpectedPrice())
                .currency(dto.getCurrency())
                .description(dto.getDescription())
                .imageUrls(dto.getImageUrls())
                // 2. Set các giá trị mặc định hệ thống
                .status(ConsignmentStatus.PENDING) // Luôn là chờ duyệt
                .build();

        // 3. Lưu vào DB
        return consignmentRepository.save(request);

    }

    public Page<ConsignmentRequest> getConsignments(ConsignmentStatus status, Pageable pageable) {
        if (status != null) {
            return consignmentRepository.findByStatus(status, pageable);
        }
        return consignmentRepository.findAll(pageable);
    }

    // 2. Cập nhật trạng thái (Duyệt/Hủy/Đã gọi)
    @Transactional
    public void updateStatus(Long id, ConsignmentStatus newStatus) {
        ConsignmentRequest request = consignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ký gửi ID: " + id));

        request.setStatus(newStatus);
        consignmentRepository.save(request);
    }

    // 3. Xóa yêu cầu (Nếu spam)
    @Transactional
    public void deleteConsignment(Long id) {
        consignmentRepository.deleteById(id);
    }
}