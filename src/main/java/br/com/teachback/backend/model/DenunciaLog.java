package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table
public class DenunciaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime acessadoEm;

    @ManyToOne
    @JoinColumn(name = "denuncia_id",nullable = false)
    private Denuncia denuncia;

    @ManyToOne
    @JoinColumn(name = "moderador_id",nullable = false)
    private Usuario moderador;
}
