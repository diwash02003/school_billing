package com.example.demo.services;

import com.example.demo.converter.StudentConverter;
import com.example.demo.dtos.StudentDTO;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentConverter studentConverter;

    public List<StudentDTO> getAllStudents() {
        List<Student> studentList = studentRepository.findByIsActiveTrue();
        return studentConverter.toDto(studentList);
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findByIdWithPayments(id).orElseThrow(() -> new RuntimeException("not found"));
        return studentConverter.toDto(student);
    }

    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = studentConverter.toEntity(studentDTO);
        Student savedStudent = studentRepository.save(student);
        return studentConverter.toDto(savedStudent);
    }

    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student existing = findStudentById(id);
        studentConverter.updateEntityFromDTO(existing, studentDTO);

        Student saved = studentRepository.save(existing);
        return studentConverter.toDto(saved);
    }

    @Transactional
    public boolean deleteStudent(Long id) {
        Student student = findStudentById(id);
        student.setIsActive(false);
        return true;
    }


    private Student findStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
