package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"enquete_id", "aluno_id"}))
public class EnqueteParticipacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, updatable=false)
    @CreationTimestamp
    private LocalDateTime respondidoEm;

    @ManyToOne
    @JoinColumn(name = "enquete_id", nullable=false)
    private Enquete enquete;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable=false)
    private Usuario aluno;
}
