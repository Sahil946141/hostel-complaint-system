package com.hostel.complaintsystem.service;

import com.hostel.complaintsystem.dto.ComplaintRequest;
import com.hostel.complaintsystem.dto.ComplaintResponse;
import com.hostel.complaintsystem.dto.StatusUpdateRequest;
import com.hostel.complaintsystem.model.Complaint;
import com.hostel.complaintsystem.model.User;
import com.hostel.complaintsystem.repository.ComplaintRepository;
import com.hostel.complaintsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintResponse createComplaint(ComplaintRequest request, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint.Category category;
        try {
            category = Complaint.Category.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            category = Complaint.Category.OTHER;
        }

        Complaint.Priority priority = Complaint.Priority.MEDIUM;
        if (request.getPriority() != null) {
            try {
                priority = Complaint.Priority.valueOf(request.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                priority = Complaint.Priority.MEDIUM;
            }
        }

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .priority(priority)
                .student(student)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getMyComplaints(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return complaintRepository.findByStudent(student)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));
        return mapToResponse(complaint);
    }

    public ComplaintResponse updateStatus(Long id, StatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        try {
            complaint.setStatus(Complaint.Status.valueOf(request.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + request.getStatus());
        }

        if (request.getRemarks() != null) {
            complaint.setAdminRemarks(request.getRemarks());
        }

        if (request.getAssignedToId() != null) {
            User staff = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            complaint.setAssignedTo(staff);
        }

        return mapToResponse(complaintRepository.save(complaint));
    }

    public List<ComplaintResponse> getComplaintsByStatus(String status) {
        try {
            Complaint.Status s = Complaint.Status.valueOf(status.toUpperCase());
            return complaintRepository.findByStatus(s)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .complaintId(complaint.getComplaintId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory().name())
                .status(complaint.getStatus().name())
                .priority(complaint.getPriority().name())
                .studentName(complaint.getStudent().getName())
                .studentEmail(complaint.getStudent().getEmail())
                .assignedToName(complaint.getAssignedTo() != null ? complaint.getAssignedTo().getName() : null)
                .adminRemarks(complaint.getAdminRemarks())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .build();
    }
}
