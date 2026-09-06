package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table
@Getter
@Setter
public class Faculdade {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false, length=20)
    private String sigla;

    @Column(length=20)
    private String exemploTurma;

    @OneToMany(mappedBy = "faculdade", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private List<FaculdadeDominio> dominios;
}
