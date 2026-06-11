package com.fd.management.backend.service;

import com.fd.management.backend.dto.BankDetailsRequest;
import com.fd.management.backend.dto.EmergencyContactDto;
import com.fd.management.backend.dto.StaffRequest;
import com.fd.management.backend.entity.EmergencyContact;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
    }

    @Transactional
    public Staff updateBankDetails(Long id, BankDetailsRequest request) {
        Staff staff = getStaffById(id);
        staff.setBankName(request.getBankName());
        staff.setBranchName(request.getBranchName());
        staff.setAccountName(request.getAccountName());
        staff.setAccountNumber(request.getAccountNumber());
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff addEmergencyContact(Long id, EmergencyContactDto request) {
        Staff staff = getStaffById(id);

        EmergencyContact contact = new EmergencyContact();
        contact.setName(request.getName());
        contact.setRelationship(request.getRelationship());
        contact.setContactNumber(request.getContactNumber());
        contact.setStaff(staff); // Staff කෙනාට ලින්ක් කරනවා

        staff.getEmergencyContacts().add(contact);
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff addStaff(StaffRequest request, String cvUrl) {
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
        staff.setCvUrl(cvUrl);

        // 🔴 මෙන්න මේක තමයි අලුතින් ආපු වැදගත්ම කෑල්ල (Password Encryption)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            // Frontend එකෙන් පාස්වර්ඩ් එකක් ආවේ නැත්නම් Default Password එකක් දානවා
            staff.setPassword(passwordEncoder.encode("fdhealth123"));
        }

        staff.setBankName(request.getBankName());
        staff.setBranchName(request.getBranchName());
        staff.setAccountName(request.getAccountName());
        staff.setAccountNumber(request.getAccountNumber());

        // QR Code එක Generate කරන කෑල්ල
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

    public Staff loginUser(String email, String rawPassword) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        if (!passwordEncoder.matches(rawPassword, staff.getPassword())) {
            throw new RuntimeException("Invalid password! Please try again.");
        }

        return staff;
    }

    @Transactional
    public void updateFcmToken(Long staffId, String token) {
        Staff staff = getStaffById(staffId);
        staff.setFcmToken(token);
        staffRepository.save(staff);
    }

    // --- පවතින Staff කෙනෙක්ගේ විස්තර Update කිරීම ---
    @Transactional
    public Staff updateStaff(Long id, StaffRequest request) {
        // 1. ඉන්න කෙනාව හොයාගන්නවා
        Staff staff = getStaffById(id);

        // 2. අලුත් විස්තර ටික තියෙනවා නම් ඒක Update කරනවා
        if (request.getFirstName() != null) staff.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) staff.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) staff.setLastName(request.getLastName());

        // Full Name එකත් අලුතින් හදලා සේව් කරනවා
        staff.setFullName(staff.getFirstName() + " " + (staff.getLastName() != null ? staff.getLastName() : ""));

        if (request.getEmail() != null) staff.setEmail(request.getEmail());
        if (request.getPhone() != null) staff.setPhone(request.getPhone());
        if (request.getRole() != null) staff.setRole(request.getRole());
        if (request.getNic() != null) staff.setNic(request.getNic());
        if (request.getMobileNo() != null) staff.setMobileNo(request.getMobileNo());
        if (request.getWhatsappNo() != null) staff.setWhatsappNo(request.getWhatsappNo());
        if (request.getAddress() != null) staff.setAddress(request.getAddress());

        // Bank Details ටික Update කරනවා
        if (request.getBankName() != null) staff.setBankName(request.getBankName());
        if (request.getBranchName() != null) staff.setBranchName(request.getBranchName());
        if (request.getAccountName() != null) staff.setAccountName(request.getAccountName());
        if (request.getAccountNumber() != null) staff.setAccountNumber(request.getAccountNumber());

        // 3. Database එකට Save කරනවා
        return staffRepository.save(staff);
    }
}