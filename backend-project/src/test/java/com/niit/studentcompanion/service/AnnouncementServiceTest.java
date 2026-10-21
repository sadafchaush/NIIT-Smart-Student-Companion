package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.niit.studentcompanion.entity.Announcement;
import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AnnouncementRepository;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Test
    void rejectsAnnouncementFromFacultyNotAssignedToSubject() {
        Faculty assignedFaculty = new Faculty();
        assignedFaculty.setFacultyId(7);
        Subject subject = new Subject();
        subject.setSubjectId(2);
        subject.setSemester(3);
        subject.setFaculty(assignedFaculty);
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
        AnnouncementService service = new AnnouncementService(
                announcementRepository, subjectRepository, facultyRepository);
        Announcement announcement = new Announcement();
        announcement.setSubjectId(2L);
        announcement.setFacultyId(8L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createAnnouncement(announcement));
        verifyNoInteractions(announcementRepository);
    }

    @Test
    void doesNotExposeAnnouncementsWithOrphanedReferences() {
        Announcement orphan = new Announcement();
        orphan.setSubjectId(2L);
        orphan.setFacultyId(7L);
        when(announcementRepository.findAll()).thenReturn(List.of(orphan));
        when(subjectRepository.findById(2)).thenReturn(Optional.empty());
        AnnouncementService service = new AnnouncementService(
                announcementRepository, subjectRepository, facultyRepository);

        assertEquals(List.of(), service.getAllAnnouncements());
    }
}
