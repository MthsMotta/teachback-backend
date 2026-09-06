package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false, unique=true, columnDefinition = "CHAR(36)")
    private String codigoConvite;

    @ManyToOne
    @JoinColumn(name = "faculdade_id", nullable=false)
    private Faculdade faculdade;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private StatusGrupo status;

    @Column(nullable=false, updatable=false)
    @CreationTimestamp
    private LocalDateTime criadoEm;

    @Column(nullable=false)
    @UpdateTimestamp
    private LocalDateTime ultimaAtividadeEm;

    @Column(nullable=false)
    @UpdateTimestamp
    private LocalDateTime atualizadoEm;
}
