package com.example.demo.converter;

import com.example.demo.dtos.StudentDTO;
import com.example.demo.models.Student;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diwash
 * @created 12/20/25
 */

@Component
public class StudentConverter {
    // Conversion methods
    public StudentDTO toDto(Student student) {
        if (student == null) return null;
        return StudentDTO.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .guardianName(student.getGuardianName())
                .phone(student.getPhone())
                .email(student.getEmail())
                .address(student.getAddress())
                .studentClass(student.getStudentClass())
                .admissionDate(student.getAdmissionDate())
                .admissionFee(student.getAdmissionFee())
                .monthlyFee(student.getMonthlyFee())
                .transportFee(student.getTransportFee())
                .previousDue(student.getPreviousDue())
                .notes(student.getNotes())
                .build();
    }

    public Student toEntity(StudentDTO dto) {
        return Student.builder()
                .id(dto.getId() != null ? dto.getId() : null)
                .fullName(dto.getFullName())
                .guardianName(dto.getGuardianName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .studentClass(dto.getStudentClass())
                .admissionDate(dto.getAdmissionDate())
                .admissionFee(dto.getAdmissionFee())
                .monthlyFee(dto.getMonthlyFee())
                .transportFee(dto.getTransportFee())
                .previousDue(dto.getPreviousDue())
                .notes(dto.getNotes())
                .isActive(true)
                .build();

    }

    public List<StudentDTO> toDto(List<Student> list) {
        if (list.isEmpty()) return new ArrayList<>();
        return list.stream()
                .map(this::toDto)
                .toList();
    }

    public void updateEntityFromDTO(Student student, StudentDTO dto) {
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
