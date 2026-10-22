package com.hostel.complaintsystem.controller;

import com.hostel.complaintsystem.model.User;
import com.hostel.complaintsystem.repository.ComplaintRepository;
import com.hostel.complaintsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    // GET /api/admin/dashboard — Dashboard stats
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComplaints", complaintRepository.count());
        stats.put("pendingComplaints", complaintRepository.findByStatus(
                com.hostel.complaintsystem.model.Complaint.Status.PENDING).size());
        stats.put("inProgressComplaints", complaintRepository.findByStatus(
                com.hostel.complaintsystem.model.Complaint.Status.IN_PROGRESS).size());
        stats.put("resolvedComplaints", complaintRepository.findByStatus(
                com.hostel.complaintsystem.model.Complaint.Status.RESOLVED).size());
        stats.put("totalStudents", userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT).count());
        stats.put("totalStaff", userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STAFF).count());
        return ResponseEntity.ok(stats);
    }

    // GET /api/admin/staff — Get all staff members
    @GetMapping("/staff")
    public ResponseEntity<List<Map<String, Object>>> getAllStaff() {
        List<Map<String, Object>> staffList = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STAFF)
                .map(u -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("id", u.getId());
                    s.put("name", u.getName());
                    s.put("email", u.getEmail());
                    return s;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(staffList);
    }

    // GET /api/admin/users — Get all users
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> um = new HashMap<>();
                    um.put("id", u.getId());
                    um.put("name", u.getName());
                    um.put("email", u.getEmail());
                    um.put("role", u.getRole().name());
                    um.put("roomNumber", u.getRoomNumber());
                    um.put("createdAt", u.getCreatedAt());
                    return um;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
