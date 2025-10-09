package com.example.demo.controllers;

import com.example.demo.dtos.StudentDTO;
import com.example.demo.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        System.out.println("Creating student with data: " + studentDTO);
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        return ResponseEntity.ok(createdStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDTO studentDTO) {
        return studentService.updateStudent(id, studentDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        boolean deleted = studentService.deleteStudent(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
