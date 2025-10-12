package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author diwash
 * @date 10/11/25
 * @description This file contains...
 */

@Entity
@Table(name = "sequence_generator")
@Getter
@Setter
@NoArgsConstructor
public class SequenceGenerator {

    @Id
    @Column(name = "sequence_name", length = 100)
    private String sequenceName;

    @Column(name = "next_value", nullable = false)
    private Long nextValue = 1001L;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SequenceGenerator(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public SequenceGenerator(String sequenceName, Long nextValue) {
        this.sequenceName = sequenceName;
        this.nextValue = nextValue;
    }
}
