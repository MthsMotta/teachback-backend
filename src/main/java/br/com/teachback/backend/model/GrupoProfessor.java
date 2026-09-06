package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_id", "professor_id"}))
public class GrupoProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable=false)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable=false)
    private Usuario professor;

    @Column(nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    private RoleGrupo role;
}
