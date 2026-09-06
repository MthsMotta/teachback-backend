package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"resposta_id", "pergunta_id"}))
public class RespostaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resposta_id", nullable=false)
    private Resposta resposta;

    @ManyToOne
    @JoinColumn(name = "pergunta_id", nullable=false)
    private Pergunta pergunta;

    @Column(nullable=false)
    private byte nota;
}
