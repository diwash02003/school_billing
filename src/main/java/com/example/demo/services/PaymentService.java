package com.example.demo.services;

import com.example.demo.dtos.PaymentFormDataDTO;
import com.example.demo.dtos.PaymentRequestDTO;
import com.example.demo.dtos.PaymentResponseDTO;
import com.example.demo.dtos.PaymentStatusDTO;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Payment;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final SequenceService sequenceService;

    public PaymentResponseDTO createInvoice(PaymentRequestDTO req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new PaymentValidationException("Student not found"));

        // Prevent duplicate admission fee invoice
        if (req.getAdmissionFee() != null && req.getAdmissionFee() > 0) {
            boolean admissionAlreadyInvoiced = paymentRepository.findByStudentIdOrderByPaymentDateDesc(student.getId())
                    .stream()
                    .anyMatch(Payment::getIsAdmissionPaid);
            if (admissionAlreadyInvoiced) {
                throw new PaymentValidationException("Admission fee already invoiced for this student");
            }
        }

        // Prevent duplicate month invoices
        List<String> paidMonths = paymentRepository.findAllByStudentId(student.getId())
                .stream()
                .flatMap(p -> p.getMonths().stream())
                .toList();
        List<String> duplicateMonths = req.getMonths().stream()
                .filter(paidMonths::contains)
                .collect(Collectors.toList());
        if (!duplicateMonths.isEmpty()) {
            throw new PaymentValidationException("Months already invoiced: " + String.join(", ", duplicateMonths));
        }

        Payment payment = convertToEntity(req, student);
        payment.setInvoiceNo(sequenceService.generateInvoiceNo());
        payment.setPreviousDue(student.getPreviousDue() == null ? 0.0 : student.getPreviousDue());
        payment.calculateTotals();

        Payment saved = paymentRepository.save(payment);

        // 🔹 Update student's previous due
        double newDue = saved.getGrandTotal(); // Grand total = totalAmount + previousDue
        student.setPreviousDue(newDue);
        return convertToDTO(saved);
    }

    public Optional<PaymentResponseDTO> getPaymentById(Long id) {
        return paymentRepository.findByIdWithStudent(id)
                .map(this::convertToDTO);
    }

    public List<PaymentResponseDTO> getPaymentsByStudentId(Long studentId) {
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PaymentResponseDTO convertToDTO(Payment p) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(p.getId());
        dto.setInvoiceNo(p.getInvoiceNo());
        dto.setStudentId(p.getStudent().getId());
        dto.setStudentName(p.getStudent().getFullName());
        dto.setStudentClass(p.getStudent().getStudentClass());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setAdmissionFee(p.getAdmissionFee());
        dto.setMonthlyFee(p.getMonthlyFee());
        dto.setTransportFee(p.getTransportFee());
        dto.setOthersFee(p.getOthersFee());
        dto.setOthersNote(p.getOthersNote());
        dto.setMonths(p.getMonths());
        dto.setTotalAmount(p.getTotalAmount());
        dto.setPreviousDue(p.getPreviousDue());
        dto.setGrandTotal(p.getGrandTotal());
        dto.setStatus(p.getStatus());
        dto.setIsAdmissionPaid(p.getIsAdmissionPaid());
        return dto;
    }

    private Payment convertToEntity(PaymentRequestDTO dto, Student student) {
        Payment p = new Payment();
        p.setStudent(student);
        p.setPaymentDate(dto.getPaymentDate());
        p.setAdmissionFee(dto.getAdmissionFee());
        p.setMonthlyFee(dto.getMonthlyFee());
        p.setTransportFee(dto.getTransportFee());
        p.setOthersFee(dto.getOthersFee());
        p.setOthersNote(dto.getOthersNote());
        p.setMonths(dto.getMonths() == null ? List.of() : List.copyOf(dto.getMonths()));
        return p;
    }

    public PaymentStatusDTO getPaymentStatus(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new PaymentValidationException("Student not found");
        }
        Student student = studentOpt.get();
        boolean hasPaidAdmission = paymentRepository.existsByStudentIdAndAdmissionFeePaid(studentId);
        List<String> paidMonths = getPaidMonths(studentId);
        List<String> availableMonths = NEPALI_MONTHS.stream().filter(month -> !paidMonths.contains(month)).collect(Collectors.toList());
        return new PaymentStatusDTO(hasPaidAdmission, paidMonths, availableMonths, student.getPreviousDue());
    }

    public PaymentFormDataDTO getPaymentFormData(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new PaymentValidationException("Student not found");
        }
        Student student = studentOpt.get();
        PaymentStatusDTO status = getPaymentStatus(studentId);
        PaymentFormDataDTO formData = new PaymentFormDataDTO();
        formData.setStudentId(studentId);
        formData.setStudentName(student.getFullName());
        formData.setStudentClass(student.getStudentClass());
        formData.setPreviousDue(student.getPreviousDue());
        formData.setAdmissionFee(student.getAdmissionFee());
        formData.setMonthlyFee(student.getMonthlyFee());
        formData.setTransportFee(student.getTransportFee());
        formData.setHasPaidAdmission(status.isHasPaidAdmission());
        formData.setAvailableMonths(status.getAvailableMonths());
        formData.setPaidMonths(status.getPaidMonths());
        return formData;
    }

    List<String> getPaidMonths(Long studentId) {
        List<Payment> payments = paymentRepository.findAllByStudentId(studentId);
        List<String> allPaidMonths = new ArrayList<>();
        for (Payment payment : payments) {
            allPaidMonths.addAll(payment.getMonths());
        }
        return allPaidMonths;
    }

    private final List<String> NEPALI_MONTHS = List.of(
            "Baishakh",
            "Jestha",
            "Ashadh",
            "Shrawan",
            "Bhadra",
            "Asoj",
            "Kartik",
            "Mangsir",
            "Poush",
            "Magh",
            "Falgun",
            "Chaitra"
    );
}