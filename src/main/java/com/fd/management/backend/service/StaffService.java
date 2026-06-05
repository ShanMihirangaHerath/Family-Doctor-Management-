package com.fd.management.backend.service;

import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    public Staff addStaff(Staff staff) {
        staff.setQrCodeString(UUID.randomUUID().toString());
        return staffRepository.save(staff);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}