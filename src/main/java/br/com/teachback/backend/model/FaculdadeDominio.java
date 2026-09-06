package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class FaculdadeDominio {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "faculdade_id", nullable = false)
    private Faculdade faculdade;

    @Column(nullable = false, length = 100, unique = true)
    private String dominio;
}
