package com.niit.studentcompanion.repository;

import com.niit.studentcompanion.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    java.util.List<Announcement> findBySemester(Integer semester);

    java.util.List<Announcement> findByFacultyId(Long facultyId);

    boolean existsBySubjectId(Long subjectId);

    boolean existsByFacultyId(Long facultyId);
}