package com.niit.studentcompanion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.SubjectRepository;
import com.niit.studentcompanion.repository.AnnouncementRepository;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private NoteRepository noteRepository;


    // Add Subject
    public Subject addSubject(Subject subject) {

        if (subject.getFaculty() != null
                && subject.getFaculty().getFacultyId() != null) {

            Integer facultyId =
                    subject.getFaculty().getFacultyId();

            Faculty faculty = facultyRepository
                    .findById(facultyId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Faculty not found with ID: "
                                    + facultyId));

            subject.setFaculty(faculty);
        }

        return subjectRepository.save(subject);
    }


    // Get All Subjects
    public List<Subject> getAllSubjects() {

        return subjectRepository.findAll();
    }

    public List<Subject> getSubjectsBySemester(Integer semester) {
        return subjectRepository.findBySemester(semester);
    }

    public List<Subject> getSubjectsByFaculty(Integer facultyId) {
        return subjectRepository.findByFacultyFacultyId(facultyId);
    }


    // Get Subject By ID
    public Subject getSubjectById(Integer id) {

        return subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found with ID: "
                                + id));
    }


    // Update Subject
    public Subject updateSubject(
            Integer id,
            Subject subjectDetails) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found with ID: "
                                + id));


        subject.setSubjectName(
                subjectDetails.getSubjectName());

        subject.setSubjectCode(
                subjectDetails.getSubjectCode());

        subject.setSemester(
                subjectDetails.getSemester());


        // Update Faculty
        if (subjectDetails.getFaculty() != null
                && subjectDetails
                .getFaculty()
                .getFacultyId() != null) {

            Integer facultyId =
                    subjectDetails
                    .getFaculty()
                    .getFacultyId();


            Faculty faculty = facultyRepository
                    .findById(facultyId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Faculty not found with ID: "
                                    + facultyId));


            subject.setFaculty(faculty);
        }


        return subjectRepository.save(subject);
    }


    // Delete Subject
    public void deleteSubject(Integer id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subject not found with ID: "
                                + id));

        Long subjectId = id.longValue();
        if (assignmentRepository.existsBySubjectId(subjectId)
                                || announcementRepository.existsBySubjectId(subjectId)
                                || noteRepository.existsBySubjectId(subjectId)) {
            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Subject is still referenced by assignments, announcements, or notes");
        }
        subjectRepository.delete(subject);
    }
}