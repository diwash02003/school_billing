package com.example.demo.services;

import com.example.demo.dtos.InvoiceFormDataDTO;
import com.example.demo.dtos.InvoiceRequestDTO;
import com.example.demo.dtos.InvoiceResponseDTO;
import com.example.demo.dtos.InvoiceStatusDTO;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Invoice;
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
public class InvoiceService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final SequenceService sequenceService;

    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new PaymentValidationException("Student not found"));

        // Prevent duplicate admission fee invoice
        if (req.getAdmissionFee() != null && req.getAdmissionFee() > 0) {
            boolean admissionAlreadyInvoiced = paymentRepository.findByStudentIdOrderByPaymentDateDesc(student.getId())
                    .stream()
                    .anyMatch(Invoice::getIsAdmissionPaid);
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

        Invoice invoice = convertToEntity(req, student);
        invoice.setInvoiceNo(sequenceService.generateInvoiceNo());
        invoice.setPreviousDue(student.getPreviousDue() == null ? 0.0 : student.getPreviousDue());
        invoice.calculateTotals();

        Invoice saved = paymentRepository.save(invoice);

        // 🔹 Update student's previous due
        double newDue = saved.getGrandTotal(); // Grand total = totalAmount + previousDue
        student.setPreviousDue(newDue);
        return convertToDTO(saved);
    }

    public Optional<InvoiceResponseDTO> getPaymentById(Long id) {
        return paymentRepository.findByIdWithStudent(id)
                .map(this::convertToDTO);
    }

    public List<InvoiceResponseDTO> getPaymentsByStudentId(Long studentId) {
        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private InvoiceResponseDTO convertToDTO(Invoice p) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
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

    private Invoice convertToEntity(InvoiceRequestDTO dto, Student student) {
        Invoice p = new Invoice();
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

    public InvoiceStatusDTO getPaymentStatus(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new PaymentValidationException("Student not found");
        }
        Student student = studentOpt.get();
        boolean hasPaidAdmission = paymentRepository.existsByStudentIdAndAdmissionFeePaid(studentId);
        List<String> paidMonths = getPaidMonths(studentId);
        List<String> availableMonths = NEPALI_MONTHS.stream().filter(month -> !paidMonths.contains(month)).collect(Collectors.toList());
        return new InvoiceStatusDTO(hasPaidAdmission, paidMonths, availableMonths, student.getPreviousDue());
    }

    public InvoiceFormDataDTO getPaymentFormData(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new PaymentValidationException("Student not found");
        }
        Student student = studentOpt.get();
        InvoiceStatusDTO status = getPaymentStatus(studentId);
        InvoiceFormDataDTO formData = new InvoiceFormDataDTO();
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
        List<Invoice> invoices = paymentRepository.findAllByStudentId(studentId);
        List<String> allPaidMonths = new ArrayList<>();
        for (Invoice invoice : invoices) {
            allPaidMonths.addAll(invoice.getMonths());
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