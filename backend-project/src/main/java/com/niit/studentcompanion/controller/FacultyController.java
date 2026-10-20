package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.service.FacultyService;

@RestController
@RequestMapping("/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;


    // Add Faculty
    @PostMapping
    public Faculty addFaculty(@RequestBody Faculty faculty) {

        return facultyService.addFaculty(faculty);
    }


    // Get All Faculty
    @GetMapping
    public List<Faculty> getAllFaculty() {

        return facultyService.getAllFaculty();
    }


    // Get Faculty By ID
    @GetMapping("/{id}")
    public Faculty getFacultyById(@PathVariable Integer id) {

        return facultyService.getFacultyById(id);
    }


    // Update Faculty
    @PutMapping("/{id}")
    public Faculty updateFaculty(
            @PathVariable Integer id,
            @RequestBody Faculty faculty) {

        return facultyService.updateFaculty(id, faculty);
    }


    // Delete Faculty
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFaculty(
            @PathVariable Integer id) {

        facultyService.deleteFaculty(id);

        return ResponseEntity.ok(
                "Faculty deleted successfully"
        );
    }
}