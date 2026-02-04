package com.portfolio.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "experiences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String role;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isCurrent;
    
    @Column(name = "\"order\"")
    private Integer order;
}
