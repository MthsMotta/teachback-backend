package br.com.teachback.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table
@Getter
@Setter
public class Usuario implements UserDetails {

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
    @UpdateTimestamp
    private LocalDateTime atualizadoEm;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != StatusUsuario.SUSPENSO;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return status == StatusUsuario.ATIVO;
    }
}
