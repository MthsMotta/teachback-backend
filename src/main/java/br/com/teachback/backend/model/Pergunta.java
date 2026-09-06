package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=500, nullable=false)
    private String texto;

    @Column(nullable=false)
    private byte ordem;

    @Column(nullable=false)
    private boolean ativa;
}
