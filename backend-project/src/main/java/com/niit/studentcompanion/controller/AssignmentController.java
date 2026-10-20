package com.niit.studentcompanion.controller;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.service.AssignmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // Constructor Injection
    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // CREATE ASSIGNMENT
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestBody Assignment assignment) {

        Assignment savedAssignment =
                assignmentService.createAssignment(assignment);

        return new ResponseEntity<>(
                savedAssignment,
                HttpStatus.CREATED
        );
    }

    // GET ALL ASSIGNMENTS
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments(
            @RequestParam(required = false) Integer semester) {

        List<Assignment> assignments =
                assignmentService.getAssignmentsBySemester(semester);

        return ResponseEntity.ok(assignments);
    }

    // GET ASSIGNMENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(
            @PathVariable Long id) {

        return assignmentService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE ASSIGNMENT
    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignment) {

        try {

            Assignment updatedAssignment =
                    assignmentService.updateAssignment(
                            id,
                            assignment
                    );

            return ResponseEntity.ok(updatedAssignment);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    // DELETE ASSIGNMENT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id) {

        try {

            assignmentService.deleteAssignment(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }
}