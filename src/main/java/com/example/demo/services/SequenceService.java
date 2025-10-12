package com.example.demo.services;

import com.example.demo.models.SequenceGenerator;
import com.example.demo.repositories.SequenceGeneratorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */


@Service
@Transactional
@RequiredArgsConstructor
public class SequenceService {
    private final SequenceGeneratorRepository sequenceGeneratorRepository;

    public String generateInvoiceNo() {
        Long seq = getNextSequenceValue("invoice_seq");
        return String.format("INV-%d-%04d", Year.now().getValue(), seq);
    }

    public String generateReceiptNo() {
        Long seq = getNextSequenceValue("receipt_seq");
        return String.format("RCT-%d-%04d", Year.now().getValue(), seq);
    }

    private Long getNextSequenceValue(String sequenceName) {
        // Try to get existing sequence
        Optional<SequenceGenerator> sequenceOpt = sequenceGeneratorRepository.findByNameForUpdate(sequenceName);

        if (sequenceOpt.isPresent()) {
            // Increment existing sequence
            SequenceGenerator sequence = sequenceOpt.get();
            Long currentValue = sequence.getNextValue();
            sequence.setNextValue(currentValue + 1);
            sequence.setUpdatedAt(LocalDateTime.now());
            sequenceGeneratorRepository.save(sequence);
            return currentValue;
        } else {
            // Create new sequence starting from 1001
            SequenceGenerator newSequence = new SequenceGenerator(sequenceName, 1001L);
            sequenceGeneratorRepository.save(newSequence);
            return 1001L;
        }
    }

    // Optional: Method to initialize sequences on application start
    @PostConstruct
    public void initializeSequences() {
        initializeSequence("receipt_seq");
        initializeSequence("invoice_seq");
    }

    private void initializeSequence(String sequenceName) {
        if (!sequenceGeneratorRepository.existsById(sequenceName)) {
            sequenceGeneratorRepository.save(new SequenceGenerator(sequenceName, 1001L));
        }
    }
}