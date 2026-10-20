
package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.service.AttendanceService;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;


    @PostMapping
    public Attendance addAttendance(
            @RequestBody Attendance attendance) {

        return attendanceService
                .addAttendance(attendance);
    }


    @GetMapping
    public List<Attendance> getAllAttendance() {

        return attendanceService
                .getAllAttendance();
    }
    
    @GetMapping("/student/{studentId}")
    public List<Attendance> getAttendanceByStudent(
            @PathVariable Long studentId) {

        return attendanceService
                .getAttendanceByStudent(studentId);
    }


    @GetMapping("/{id}")
    public Attendance getAttendanceById(
            @PathVariable Integer id) {

        return attendanceService
                .getAttendanceById(id);
    }


    @PutMapping("/{id}")
    public Attendance updateAttendance(
            @PathVariable Integer id,
            @RequestBody Attendance attendance) {

        return attendanceService
                .updateAttendance(id, attendance);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Integer id) {

        attendanceService
                .deleteAttendance(id);

        return ResponseEntity.ok(
                "Attendance deleted successfully");
    }
}