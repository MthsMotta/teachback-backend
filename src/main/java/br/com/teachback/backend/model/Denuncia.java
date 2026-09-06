package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"resposta_id", "professor_id"}))
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusDenuncia status;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    @Column(columnDefinition = "TEXT")
    private String comentarioAnalise;

    @ManyToOne
    @JoinColumn(name = "resposta_id", nullable = false)
    private Resposta resposta;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime criadoEm;
}
