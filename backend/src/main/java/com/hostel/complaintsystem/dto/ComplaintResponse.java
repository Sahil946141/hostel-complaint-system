package com.hostel.complaintsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private String complaintId;
    private String title;
    private String description;
    private String category;
    private String status;
    private String priority;
    private String studentName;
    private String studentEmail;
    private String assignedToName;
    private String adminRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
