package com.niit.studentcompanion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niit.studentcompanion.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Optional<Notification> findByIdAndStudentId(Long id, Long studentId);

    List<Notification> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);

    Optional<Notification> findByIdAndFacultyId(Long id, Long facultyId);
}
