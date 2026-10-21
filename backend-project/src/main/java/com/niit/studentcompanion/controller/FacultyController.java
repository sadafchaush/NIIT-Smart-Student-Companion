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
import com.niit.studentcompanion.dto.FacultyResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import com.niit.studentcompanion.repository.FacultyRepository;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private FacultyRepository facultyRepository;

    // Add Faculty
    @PostMapping
    public FacultyResponse addFaculty(@RequestBody Faculty faculty) {

        return FacultyResponse.from(facultyService.addFaculty(faculty));
    }


    // Get All Faculty
    @GetMapping
    public List<FacultyResponse> getAllFaculty(Authentication authentication) {
        List<Faculty> facultyList;
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_ADMIN")) {
            facultyList = facultyService.getAllFaculty();
        } else {
            facultyList = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return facultyList.stream()
                .map(FacultyResponse::from)
                .toList();
    }


    // Get Faculty By ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.isOwnFacultyId(#p0, authentication)")
    public FacultyResponse getFacultyById(@PathVariable Integer id) {

        return FacultyResponse.from(facultyService.getFacultyById(id));
    }


    // Update Faculty
    @PutMapping("/{id}")
    public FacultyResponse updateFaculty(
            @PathVariable Integer id,
            @RequestBody Faculty faculty) {

        return FacultyResponse.from(facultyService.updateFaculty(id, faculty));
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