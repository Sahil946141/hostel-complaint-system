package com.hostel.complaintsystem.controller;

import com.hostel.complaintsystem.dto.ComplaintRequest;
import com.hostel.complaintsystem.dto.ComplaintResponse;
import com.hostel.complaintsystem.dto.StatusUpdateRequest;
import com.hostel.complaintsystem.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;

    // POST /api/complaints — Raise a new complaint (Student)
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody ComplaintRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(complaintService.createComplaint(request, email));
    }

    // GET /api/complaints — Get all complaints (Admin/Staff)
    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    // GET /api/complaints/my — Get logged-in student's complaints
    @GetMapping("/my")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(complaintService.getMyComplaints(email));
    }

    // GET /api/complaints/{id} — Get complaint by ID
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    // PUT /api/complaints/{id}/status — Update status (Admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request));
    }

    // GET /api/complaints/status/{status} — Filter by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ComplaintResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(complaintService.getComplaintsByStatus(status));
    }

    // GET /api/complaints/priority/{priority} — Filter by priority (Task 5)
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ComplaintResponse>> getByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(complaintService.getComplaintsByPriority(priority));
    }

    // DELETE /api/complaints/{id} — Delete complaint (Student - own only) (Task 5)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        complaintService.deleteComplaint(id, email);
        return ResponseEntity.noContent().build();
    }
}
