package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=36, columnDefinition = "CHAR(36)")
    private String respostaToken;

    @Column(length=500)
    private String comentario;

    @Column(length=20)
    private String turma;

    @ManyToOne
    @JoinColumn(name = "enquete_id", nullable=false)
    private Enquete enquete;

    @Column(nullable=false, updatable=false)
    @CreationTimestamp
    private LocalDateTime criadoEm;
}
