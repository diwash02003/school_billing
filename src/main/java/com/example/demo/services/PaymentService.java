package com.example.demo.services;

import com.example.demo.dtos.PaymentFormDataDTO;
import com.example.demo.dtos.PaymentRequestDTO;
import com.example.demo.dtos.PaymentResponseDTO;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.dtos.PaymentStatusDTO;
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

    private final List<String> NEPALI_MONTHS = List.of(
            "Baishakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Asoj",
            "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
    );

    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest) {
        // Validate student exists
        Student student = studentRepository.findById(paymentRequest.getStudentId())
                .orElseThrow(() -> new PaymentValidationException("Student not found"));

        // Validate admission fee payment
        if (paymentRequest.getAdmissionFee() > 0) {
            boolean hasPaidAdmission = paymentRepository.existsByStudentIdAndAdmissionFeePaid(student.getId());
            if (hasPaidAdmission) {
                throw new PaymentValidationException("Admission fee has already been paid for this student");
            }
        }

        // Validate monthly fee - check for duplicate months
        if (paymentRequest.getMonthlyFee() > 0 || paymentRequest.getTransportFee() > 0) {
            List<String> paidMonths = getPaidMonths(student.getId());
            List<String> duplicateMonths = paymentRequest.getMonths().stream()
                    .filter(paidMonths::contains)
                    .collect(Collectors.toList());

            if (!duplicateMonths.isEmpty()) {
                throw new PaymentValidationException(
                        "Fee already paid for months: " + String.join(", ", duplicateMonths));
            }
        }

        // Set calculated fields
        paymentRequest.setPreviousDue(student.getPreviousDue());
        calculatePaymentTotalsWithPartialPayment(paymentRequest);

        // Create and save payment
        Payment payment = convertToEntity(paymentRequest, student);
        Payment savedPayment = paymentRepository.save(payment);

        // Update student's previous due
        updateStudentPreviousDueWithPartialPayment(student, paymentRequest);

        return convertToDTO(savedPayment);
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

    public PaymentStatusDTO getPaymentStatus(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new PaymentValidationException("Student not found");
        }

        Student student = studentOpt.get();
        boolean hasPaidAdmission = paymentRepository.existsByStudentIdAndAdmissionFeePaid(studentId);
        List<String> paidMonths = getPaidMonths(studentId);
        List<String> availableMonths = NEPALI_MONTHS.stream()
                .filter(month -> !paidMonths.contains(month))
                .collect(Collectors.toList());

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

    // Helper method to get paid months from all payments
    private List<String> getPaidMonths(Long studentId) {
        List<Payment> payments = paymentRepository.findAllByStudentId(studentId);
        List<String> allPaidMonths = new ArrayList<>();

        for (Payment payment : payments) {
            allPaidMonths.addAll(payment.getMonths());
        }

        return allPaidMonths;
    }

    // Conversion methods
    private PaymentResponseDTO convertToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setStudentId(payment.getStudent().getId());
        dto.setStudentName(payment.getStudent().getFullName());
        dto.setStudentClass(payment.getStudent().getStudentClass());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAdmissionFee(payment.getAdmissionFee());
        dto.setMonthlyFee(payment.getMonthlyFee());
        dto.setTransportFee(payment.getTransportFee());
        dto.setOthersFee(payment.getOthersFee());
        dto.setOthersNote(payment.getOthersNote());
        dto.setMonths(payment.getMonths());
        dto.setPreviousDue(payment.getPreviousDue());
        dto.setTotalAmount(payment.getTotalAmount());
        dto.setGrandTotal(payment.getGrandTotal());
        dto.setIsAdmissionPaid(payment.getIsAdmissionPaid());
        dto.setTotalPaidAmount(payment.getTotalPaidAmount());
        return dto;
    }

    private Payment convertToEntity(PaymentRequestDTO dto, Student student) {
        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setAdmissionFee(dto.getAdmissionFee());
        payment.setMonthlyFee(dto.getMonthlyFee());
        payment.setTransportFee(dto.getTransportFee());
        payment.setOthersFee(dto.getOthersFee());
        payment.setOthersNote(dto.getOthersNote());
        payment.setMonths(new ArrayList<>(dto.getMonths()));
        payment.setPreviousDue(dto.getPreviousDue());
        payment.setTotalAmount(dto.getTotalAmount());
        payment.setGrandTotal(dto.getGrandTotal());
        payment.setIsAdmissionPaid(dto.getAdmissionFee() > 0);
        payment.setTotalPaidAmount(dto.getTotalPaidAmount());
        return payment;
    }

    private void calculatePaymentTotalsWithPartialPayment(PaymentRequestDTO paymentRequest) {
        int monthsCount = paymentRequest.getMonths().size();
        double monthlyTotal = paymentRequest.getMonthlyFee() * monthsCount;
        double transportTotal = paymentRequest.getTransportFee() * monthsCount;

        // Calculate total fees (without previous due)
        double totalFees = paymentRequest.getAdmissionFee() + monthlyTotal + transportTotal + paymentRequest.getOthersFee();

        double totalAmountDue = totalFees + paymentRequest.getPreviousDue();

        // If paid amount is provided and less than total amount due, it's a partial payment
        if (paymentRequest.getTotalPaidAmount() != null && paymentRequest.getTotalPaidAmount() > 0) {
            if (paymentRequest.getTotalPaidAmount() > totalAmountDue) {
                throw new PaymentValidationException("Paid amount cannot be greater than total amount due");
            }
            // This is a partial payment
            paymentRequest.setTotalAmount(totalFees);
            paymentRequest.setGrandTotal(paymentRequest.getTotalPaidAmount()); // Grand total becomes the actual paid amount
        } else {
            paymentRequest.setTotalAmount(totalFees);
            paymentRequest.setGrandTotal(totalAmountDue);
            paymentRequest.setTotalPaidAmount(totalAmountDue); // If no paid amount specified, assume full payment
        }
    }

    private void updateStudentPreviousDueWithPartialPayment(Student student, PaymentRequestDTO paymentRequest) {
        double totalAmountDue = paymentRequest.getTotalAmount() + paymentRequest.getPreviousDue();

        double paidAmount = paymentRequest.getTotalPaidAmount();
        if (paidAmount < totalAmountDue) {
            // Partial payment - calculate remaining due
            double remainingDue = totalAmountDue - paidAmount;
            student.setPreviousDue(remainingDue);
        } else {
            // Full payment - no due remaining
            student.setPreviousDue(0.0);
        }
        studentRepository.save(student);
    }
}