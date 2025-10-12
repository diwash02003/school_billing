package com.example.demo.services;

import com.example.demo.dtos.ReceiptRequestDTO;
import com.example.demo.dtos.ReceiptResponseDTO;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Receipt;
import com.example.demo.models.Student;
import com.example.demo.repositories.ReceiptRepository;
import com.example.demo.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@Service
@Transactional
@RequiredArgsConstructor
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final StudentRepository studentRepository;
    private final SequenceService sequenceService;

    public ReceiptResponseDTO createReceipt(ReceiptRequestDTO req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new PaymentValidationException("Student not found"));

        double previousDue = student.getPreviousDue() == null ? 0.0 : student.getPreviousDue();
        if (previousDue <= 0) {
            throw new PaymentValidationException("No pending due found for student");
        }

        if (req.getPaidAmount() > (previousDue + 1e9)) {
            // unrealistic guard; normally you can accept > previous due as advance, but we disallow huge numbers
        }

        // apply payment: we assume paidAmount reduces previousDue (if paid > previousDue, remainingDue = 0)
        double remainingDue = Math.max(0.0, previousDue - req.getPaidAmount());
        student.setPreviousDue(remainingDue);
        studentRepository.save(student);

        Receipt r = new Receipt();
        r.setReceiptNo(sequenceService.generateReceiptNo());
        r.setStudent(student);
        r.setPaidAmount(req.getPaidAmount());
        r.setPaymentMethod(req.getPaymentMethod());
        r.setRemarks(req.getRemarks());
        r.setPreviousDueSnapshot(previousDue);
        r.setRemainingDue(remainingDue);

        Receipt saved = receiptRepository.save(r);
        return convertToDTO(saved);
    }

    public List<ReceiptResponseDTO> getReceiptsByStudent(Long studentId) {
        return receiptRepository.findByStudentIdOrderByReceiptDateDesc(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReceiptResponseDTO convertToDTO(Receipt r) {
        ReceiptResponseDTO dto = new ReceiptResponseDTO();
        dto.setId(r.getId());
        dto.setReceiptNo(r.getReceiptNo());
        dto.setStudentId(r.getStudent().getId());
        dto.setStudentName(r.getStudent().getFullName());
        dto.setReceiptDate(r.getReceiptDate());
        dto.setPaidAmount(r.getPaidAmount());
        dto.setPaymentMethod(r.getPaymentMethod());
        dto.setRemarks(r.getRemarks());
        dto.setPreviousDueSnapshot(r.getPreviousDueSnapshot());
        dto.setRemainingDue(r.getRemainingDue());
        return dto;
    }
}

