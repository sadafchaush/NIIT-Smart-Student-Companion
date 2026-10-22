package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Announcement;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.service.AnnouncementService;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public AnnouncementController(
            AnnouncementService announcementService,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository) {
        this.announcementService = announcementService;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(
            @RequestBody Announcement announcement,
            Authentication authentication) {
        announcement.setFacultyId(resolveFacultyId(
                authentication, announcement.getFacultyId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcementService.createAnnouncement(announcement));
    }

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements(
            @RequestParam(required = false) Integer semester,
            Authentication authentication) {
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT")) {
            semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            if (semester == null) {
                return ResponseEntity.ok(List.of());
            }
        }
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY")) {
            Long facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId().longValue())
                    .orElse(null);
            return ResponseEntity.ok(facultyId == null
                    ? List.of()
                    : announcementService.getAnnouncementsByFaculty(facultyId));
        }
        return ResponseEntity.ok(
                announcementService.getAnnouncementsBySemester(semester));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accountAccess.canAccessAnnouncement(#p0, authentication)")
    public ResponseEntity<?> getAnnouncementById(
            @PathVariable Long id) {
        Announcement announcement =
                announcementService.getAnnouncementById(id).orElse(null);
        if (announcement == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Announcement not found."));
        }
        return ResponseEntity.ok(announcement);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAnnouncement(#p0, authentication)")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement,
            Authentication authentication) {
        announcement.setFacultyId(resolveFacultyId(
                authentication, announcement.getFacultyId()));
        return ResponseEntity.ok(
                announcementService.updateAnnouncement(id, announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAnnouncement(#p0, authentication)")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveFacultyId(
            Authentication authentication,
            Long suppliedFacultyId) {
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElseGet(() -> {
                    if (suppliedFacultyId == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Faculty is required");
                    }
                    return suppliedFacultyId;
                });
    }
}
