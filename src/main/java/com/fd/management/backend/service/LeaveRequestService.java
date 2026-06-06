package com.fd.management.backend.service;

import com.fd.management.backend.dto.LeaveDto;
import com.fd.management.backend.entity.LeaveRequest;
import com.fd.management.backend.entity.Staff;
import com.fd.management.backend.repository.LeaveRequestRepository;
import com.fd.management.backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRepository;
    private final StaffRepository staffRepository;

    public LeaveRequest applyLeave(Long staffId, LeaveDto request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found!"));

        LeaveRequest leave = new LeaveRequest();
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStaff(staff);

        return leaveRepository.save(leave);
    }

    public List<LeaveRequest> getMyLeaves(Long staffId) {
        return leaveRepository.findByStaffIdOrderByAppliedOnDesc(staffId);
    }
}