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
public class Usuario {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String nome;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String senha;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable=false, length=30)
    @Enumerated(EnumType.STRING)
    private StatusUsuario status;

    @Column
    private LocalDateTime suspensoAte;

    @ManyToOne
    @JoinColumn(name = "faculdade_id")
    private Faculdade faculdade;

    @Column
    private LocalDateTime ultimoAcessoEm;

    @Column(nullable=false, updatable=false)
    @CreationTimestamp
    private LocalDateTime criadoEm;

    @Column(nullable=false)
    @CreationTimestamp
    @UpdateTimestamp
    private LocalDateTime atualizadoEm;
}
