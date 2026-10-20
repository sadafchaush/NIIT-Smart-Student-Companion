package com.niit.studentcompanion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SubjectRepository subjectRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
    }

    private void validateAssignmentAccess(Assignment assignment) {
        if (assignment.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (assignment.getFacultyId() == null) {
            throw new IllegalArgumentException("Faculty is required.");
        }

        Subject subject = subjectRepository.findById(
                Math.toIntExact(assignment.getSubjectId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found with id: " + assignment.getSubjectId()));

        if (subject.getFaculty() == null || subject.getFaculty().getFacultyId() == null) {
            throw new IllegalArgumentException(
                    "This subject is not assigned to any faculty yet.");
        }

        if (subject.getFaculty().getFacultyId().longValue()
                != assignment.getFacultyId()) {
            throw new IllegalArgumentException(
                    "This faculty is not assigned to the selected subject.");
        }

        if (assignment.getSemester() == null) {
            assignment.setSemester(subject.getSemester());
        }

        if (assignment.getSemester() != null && subject.getSemester() != null
                && !assignment.getSemester().equals(subject.getSemester())) {
            throw new IllegalArgumentException(
                    "Assignment semester does not match the subject semester.");
        }
    }

    public Assignment createAssignment(Assignment assignment) {
        validateAssignmentAccess(assignment);
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public List<Assignment> getAssignmentsBySemester(Integer semester) {
        if (semester == null) {
            return getAllAssignments();
        }
        return assignmentRepository.findBySemester(semester);
    }

    public Optional<Assignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public Assignment updateAssignment(Long id, Assignment updatedAssignment) {
        Assignment existingAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Assignment not found with id: " + id));

        validateAssignmentAccess(updatedAssignment);

        existingAssignment.setTitle(updatedAssignment.getTitle());
        existingAssignment.setDescription(updatedAssignment.getDescription());
        existingAssignment.setDueDate(updatedAssignment.getDueDate());
        existingAssignment.setClassName(updatedAssignment.getClassName());
        existingAssignment.setSemester(updatedAssignment.getSemester());
        existingAssignment.setSubjectId(updatedAssignment.getSubjectId());
        existingAssignment.setFacultyId(updatedAssignment.getFacultyId());

        return assignmentRepository.save(existingAssignment);
    }

    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException(
                    "Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }
}