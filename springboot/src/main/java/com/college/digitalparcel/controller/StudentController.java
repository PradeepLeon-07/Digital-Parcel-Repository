package com.college.digitalparcel.controller;

import com.college.digitalparcel.dto.CreateStudentRequest;
import com.college.digitalparcel.dto.StudentResponse;
import com.college.digitalparcel.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{registerNumber}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String registerNumber) {
        return ResponseEntity.ok(studentService.getStudentByRegisterNumber(registerNumber));
    }
}
