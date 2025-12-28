package com.example.buildingmanager.controllers;


import com.example.buildingmanager.entities.Customer;
import com.example.buildingmanager.models.customer.CustomerContactRequest;
import com.example.buildingmanager.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    // API này CÔNG KHAI (Khách không cần đăng nhập vẫn gửi được)
    @PostMapping("/contact")
    public ResponseEntity<?> sendContact(@RequestBody CustomerContactRequest request) {
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setDemand(request.getDemand());
        customer.setStatus(1); // 1: Khách mới, chờ xử lý
        customer.setIsActive(1);
        
        // Lưu ngày tạo tự động nhờ @EntityListeners trong Entity Customer
        customerRepository.save(customer);

        return ResponseEntity.ok("Gửi yêu cầu thành công! Nhân viên sẽ liên hệ sớm.");
    }
}