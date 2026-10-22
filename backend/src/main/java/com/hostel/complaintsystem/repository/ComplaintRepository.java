package com.hostel.complaintsystem.repository;

import com.hostel.complaintsystem.model.Complaint;
import com.hostel.complaintsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudent(User student);
    List<Complaint> findByStatus(Complaint.Status status);
    List<Complaint> findByAssignedTo(User staff);
    Optional<Complaint> findByComplaintId(String complaintId);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
