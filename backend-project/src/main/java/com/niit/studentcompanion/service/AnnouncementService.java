package com.niit.studentcompanion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Announcement;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AnnouncementRepository;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository) {
        this.announcementRepository = announcementRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
    }

    private void validateAnnouncementAccess(Announcement announcement) {
        if (announcement.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (announcement.getFacultyId() == null) {
            throw new IllegalArgumentException("Faculty is required.");
        }

        Subject subject = subjectRepository.findById(
                Math.toIntExact(announcement.getSubjectId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found with id: " + announcement.getSubjectId()));

        if (subject.getFaculty() == null || subject.getFaculty().getFacultyId() == null) {
            throw new IllegalArgumentException(
                    "This subject is not assigned to any faculty yet.");
        }

        if (subject.getFaculty().getFacultyId().longValue()
                != announcement.getFacultyId()) {
            throw new IllegalArgumentException(
                    "This faculty is not assigned to the selected subject.");
        }

        if (announcement.getSemester() == null) {
            announcement.setSemester(subject.getSemester());
        }

        if (announcement.getSemester() != null && subject.getSemester() != null
                && !announcement.getSemester().equals(subject.getSemester())) {
            throw new IllegalArgumentException(
                    "Announcement semester does not match the subject semester.");
        }
    }

    public Announcement createAnnouncement(Announcement announcement) {
        validateAnnouncementAccess(announcement);
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll().stream()
                .filter(this::hasValidReferences)
                .toList();
    }

    public List<Announcement> getAnnouncementsBySemester(Integer semester) {
        if (semester == null) {
            return getAllAnnouncements();
        }
        return announcementRepository.findBySemester(semester).stream()
                .filter(this::hasValidReferences)
                .toList();
    }

    public List<Announcement> getAnnouncementsByFaculty(Long facultyId) {
        return announcementRepository.findByFacultyId(facultyId).stream()
                .filter(this::hasValidReferences)
                .toList();
    }

    public Optional<Announcement> getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .filter(this::hasValidReferences);
    }

    public Announcement updateAnnouncement(
            Long id,
            Announcement updatedAnnouncement) {

        Announcement existingAnnouncement =
                announcementRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Announcement not found with id: " + id
                                )
                        );

        validateAnnouncementAccess(updatedAnnouncement);

        existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
        existingAnnouncement.setMessage(updatedAnnouncement.getMessage());
        existingAnnouncement.setAnnouncementDate(updatedAnnouncement.getAnnouncementDate());
        existingAnnouncement.setSubjectId(updatedAnnouncement.getSubjectId());
        existingAnnouncement.setSemester(updatedAnnouncement.getSemester());
        existingAnnouncement.setFacultyId(updatedAnnouncement.getFacultyId());

        return announcementRepository.save(existingAnnouncement);
    }

    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new RuntimeException(
                    "Announcement not found with id: " + id
            );
        }

        announcementRepository.deleteById(id);
    }

    private boolean hasValidReferences(Announcement announcement) {
        return announcement.getSubjectId() != null
                && announcement.getFacultyId() != null
                && announcement.getSubjectId() <= Integer.MAX_VALUE
                && announcement.getFacultyId() <= Integer.MAX_VALUE
                && subjectRepository.findById(
                        announcement.getSubjectId().intValue()).isPresent()
                && facultyRepository.findById(
                        announcement.getFacultyId().intValue()).isPresent();
    }
}