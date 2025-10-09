package com.example.demo.services;

import com.example.demo.dtos.StudentDTO;
import com.example.demo.repositories.StudentRepository;
import com.example.demo.models.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<StudentDTO> getStudentById(Long id) {
        return studentRepository.findByIdWithPayments(id)
                .map(this::convertToDTO);
    }

    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = convertToEntity(studentDTO);
        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    public Optional<StudentDTO> updateStudent(Long id, StudentDTO studentDTO) {
        return studentRepository.findById(id)
                .map(existingStudent -> {
                    updateEntityFromDTO(existingStudent, studentDTO);
                    Student updatedStudent = studentRepository.save(existingStudent);
                    return convertToDTO(updatedStudent);
                });
    }

    public boolean deleteStudent(Long id) {
        return studentRepository.findById(id)
                .map(student -> {
                    student.setIsActive(false);
                    studentRepository.save(student);
                    return true;
                })
                .orElse(false);
    }

    // Conversion methods
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setFullName(student.getFullName());
        dto.setGuardianName(student.getGuardianName());
        dto.setPhone(student.getPhone());
        dto.setEmail(student.getEmail());
        dto.setAddress(student.getAddress());
        dto.setStudentClass(student.getStudentClass());
        dto.setAdmissionDate(student.getAdmissionDate());
        dto.setAdmissionFee(student.getAdmissionFee());
        dto.setMonthlyFee(student.getMonthlyFee());
        dto.setTransportFee(student.getTransportFee());
        dto.setPreviousDue(student.getPreviousDue());
        dto.setNotes(student.getNotes());
        return dto;
    }

    private Student convertToEntity(StudentDTO dto) {
        Student student = new Student();
        updateEntityFromDTO(student, dto);
        return student;
    }

    private void updateEntityFromDTO(Student student, StudentDTO dto) {
        student.setFullName(dto.getFullName());
        student.setGuardianName(dto.getGuardianName());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        student.setAddress(dto.getAddress());
        student.setStudentClass(dto.getStudentClass());
        student.setAdmissionDate(dto.getAdmissionDate());
        student.setAdmissionFee(dto.getAdmissionFee());
        student.setMonthlyFee(dto.getMonthlyFee());
        student.setTransportFee(dto.getTransportFee());
        student.setPreviousDue(dto.getPreviousDue());
        student.setNotes(dto.getNotes());
    }
}
