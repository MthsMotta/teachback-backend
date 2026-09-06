package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
public class Enquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private StatusEnquete status;

    @Column(nullable=false, updatable=false)
    private LocalDate dataInicio;

    @Column(nullable=false, updatable=false)
    private LocalDate dataFim;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable=false)
    private Grupo grupo;

    @Column(nullable=false, updatable=false)
    @CreationTimestamp
    private LocalDateTime criadoEm;
}
