package com.hostel.complaintsystem.service;

import com.hostel.complaintsystem.model.Complaint;
import com.hostel.complaintsystem.model.User;
import com.hostel.complaintsystem.repository.ComplaintRepository;
import com.hostel.complaintsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ComplaintService complaintService;

    private User student;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .name("Student")
                .email("student@hostel.com")
                .role(User.Role.STUDENT)
                .build();
        complaint = Complaint.builder()
                .id(10L)
                .complaintId("CMP-20261022-123456")
                .title("WiFi is unavailable")
                .description("The WiFi has been unavailable in my room.")
                .category(Complaint.Category.WIFI)
                .status(Complaint.Status.PENDING)
                .priority(Complaint.Priority.HIGH)
                .student(student)
                .build();
    }

    @Test
    void filtersComplaintsByPriorityIgnoringCase() {
        when(complaintRepository.findByPriority(Complaint.Priority.HIGH))
                .thenReturn(List.of(complaint));

        var results = complaintService.getComplaintsByPriority("high");

        assertEquals(1, results.size());
        assertEquals("HIGH", results.get(0).getPriority());
        verify(complaintRepository).findByPriority(Complaint.Priority.HIGH);
    }

    @Test
    void rejectsUnknownPriority() {
        assertThrows(IllegalArgumentException.class,
                () -> complaintService.getComplaintsByPriority("urgent-ish"));
        verifyNoInteractions(complaintRepository);
    }

    @Test
    void deletesComplaintOwnedByStudent() {
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(complaint));

        complaintService.deleteComplaint(10L, "STUDENT@HOSTEL.COM");

        verify(complaintRepository).delete(complaint);
    }

    @Test
    void preventsStudentFromDeletingAnotherStudentsComplaint() {
        when(complaintRepository.findById(10L)).thenReturn(Optional.of(complaint));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> complaintService.deleteComplaint(10L, "another@hostel.com"));

        assertEquals(403, exception.getStatusCode().value());
        verify(complaintRepository).findById(10L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownComplaint() {
        when(complaintRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> complaintService.deleteComplaint(404L, "student@hostel.com"));

        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(userRepository);
    }
}
