package com.hostel.complaintsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10)
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String priority; // LOW, MEDIUM, HIGH, URGENT
}
