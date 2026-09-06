package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_id", "aluno_id"}))
public class GrupoMembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private StatusMembro status;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable=false)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable=false)
    private Usuario aluno;

    @Column(nullable=false)
    @CreationTimestamp
    private LocalDateTime entrouEm;

    @Column
    private LocalDateTime removidoEm;
}
