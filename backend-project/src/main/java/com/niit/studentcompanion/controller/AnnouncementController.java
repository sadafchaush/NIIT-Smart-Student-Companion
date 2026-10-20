package com.niit.studentcompanion.controller;

import com.niit.studentcompanion.entity.Announcement;
import com.niit.studentcompanion.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(
            AnnouncementService announcementService) {

        this.announcementService = announcementService;
    }

    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(
            @RequestBody Announcement announcement) {

        Announcement savedAnnouncement =
                announcementService.createAnnouncement(announcement);

        return new ResponseEntity<>(
                savedAnnouncement,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements(
            @RequestParam(required = false) Integer semester) {

        List<Announcement> announcements =
                announcementService.getAnnouncementsBySemester(semester);

        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(
            @PathVariable Long id) {

        return announcementService
                .getAnnouncementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement) {

        try {

            Announcement updatedAnnouncement =
                    announcementService.updateAnnouncement(
                            id,
                            announcement
                    );

            return ResponseEntity.ok(updatedAnnouncement);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id) {

        try {

            announcementService.deleteAnnouncement(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }
}