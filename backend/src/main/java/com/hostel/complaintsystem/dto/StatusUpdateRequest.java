package com.hostel.complaintsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {
    private String status;   // PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String remarks;
    private Long assignedToId;
}
