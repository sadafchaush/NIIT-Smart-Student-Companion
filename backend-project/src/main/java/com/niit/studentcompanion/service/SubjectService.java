package com.niit.studentcompanion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FacultyRepository facultyRepository;


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

        subjectRepository.delete(subject);
    }
}