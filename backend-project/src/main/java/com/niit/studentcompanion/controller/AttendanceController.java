
package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.service.AttendanceService;
import com.niit.studentcompanion.dto.AttendanceResponse;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canManageAttendanceRequest(#p0, authentication)")
    public AttendanceResponse addAttendance(
            @RequestBody Attendance attendance) {

        return AttendanceResponse.from(
                attendanceService.addAttendance(attendance));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public List<AttendanceResponse> getAllAttendance(Authentication authentication) {
        List<Attendance> attendance = AuthorityUtils
                .authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_ADMIN")
                ? attendanceService.getAllAttendance()
                : attendanceService.getAttendanceByFaculty(authentication.getName());
        return attendance.stream()
                .map(AttendanceResponse::from)
                .toList();
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY') or @accountAccess.isOwnStudentId(#p0, authentication)")
    public List<AttendanceResponse> getAttendanceByStudent(
            @PathVariable Long studentId,
            Authentication authentication) {
        List<Attendance> attendance = AuthorityUtils
                .authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_ADMIN")
                ? attendanceService.getAttendanceByStudent(studentId)
                : AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                        .contains("ROLE_FACULTY")
                        ? attendanceService.getAttendanceByStudentAndFaculty(
                                studentId, authentication.getName())
                        : attendanceService.getAttendanceByStudent(studentId);
        return attendance.stream()
                .map(AttendanceResponse::from)
                .toList();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAttendance(#p0, authentication)")
    public AttendanceResponse getAttendanceById(
            @PathVariable Integer id) {

        return AttendanceResponse.from(attendanceService.getAttendanceById(id));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canManageAttendance(#p0, #p1, authentication)")
    public AttendanceResponse updateAttendance(
            @PathVariable Integer id,
            @RequestBody Attendance attendance) {

        return AttendanceResponse.from(
                attendanceService.updateAttendance(id, attendance));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAttendance(#p0, authentication)")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Integer id) {

        attendanceService
                .deleteAttendance(id);

        return ResponseEntity.ok(
                "Attendance deleted successfully");
    }
}