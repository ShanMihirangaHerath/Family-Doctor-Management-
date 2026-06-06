package com.fd.management.backend.service;

import com.fd.management.backend.dto.EmergencyContactDto;
import com.fd.management.backend.dto.StaffRequest;
import com.fd.management.backend.entity.EmergencyContact;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    // --- අලුත් Add Staff ලොජික් එක ---
    @Transactional
    public Staff addStaff(StaffRequest request) {
        Staff staff = new Staff();

        staff.setFullName(request.getFirstName() + " " + request.getLastName());
        staff.setFirstName(request.getFirstName());
        staff.setMiddleName(request.getMiddleName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setRole(request.getRole());
        staff.setNic(request.getNic());
        staff.setMobileNo(request.getMobileNo());
        staff.setWhatsappNo(request.getWhatsappNo());
        staff.setAddress(request.getAddress());

        staff.setBankName(request.getBankName());
        staff.setBranchName(request.getBranchName());
        staff.setAccountName(request.getAccountName());
        staff.setAccountNumber(request.getAccountNumber());

        staff.setQrCodeString("FD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        if (request.getEmergencyContacts() != null) {
            for (EmergencyContactDto contactDto : request.getEmergencyContacts()) {
                EmergencyContact contact = new EmergencyContact();
                contact.setName(contactDto.getName());
                contact.setRelationship(contactDto.getRelationship());
                contact.setContactNumber(contactDto.getContactNumber());

                contact.setStaff(staff);
                staff.getEmergencyContacts().add(contact);
            }
        }

        return staffRepository.save(staff);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff loginByEmail(String email) {
        return staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));
    }
}