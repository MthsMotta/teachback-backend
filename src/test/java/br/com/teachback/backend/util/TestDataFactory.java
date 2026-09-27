package br.com.teachback.backend.util;

import br.com.teachback.backend.dto.request.CadastroRequest;
import br.com.teachback.backend.model.*;

import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static Faculdade criarFaculdade() {
        Faculdade faculdade = new Faculdade();
        faculdade.setId(1L);
        faculdade.setNome("Universidade Teste");
        faculdade.setSigla("UT");
        faculdade.setExemploTurma("02BN");
        return faculdade;
    }

    public static FaculdadeDominio criarFaculdadeDominio(Faculdade faculdade) {
        FaculdadeDominio dominio = new FaculdadeDominio();
        dominio.setId(1L);
        dominio.setFaculdade(faculdade);
        dominio.setDominio("teste.edu.br");
        return dominio;
    }

    public static Usuario criarUsuario(Role role, StatusUsuario status) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("teste@teste.edu.br");
        usuario.setSenha("senhaHasheadaFake");
        usuario.setRole(role);
        usuario.setStatus(status);
        usuario.setFaculdade(criarFaculdade());
        return usuario;
    }

    public static Usuario criarUsuarioAluno() {
        return criarUsuario(Role.ALUNO, StatusUsuario.PENDENTE_CONFIRMACAO);
    }

    public static Usuario criarUsuarioProfessor() {
        return criarUsuario(Role.PROFESSOR, StatusUsuario.PENDENTE_CONFIRMACAO);
    }

    public static CadastroRequest criarCadastroRequest(RoleCadastro tipoUsuario) {
        return new CadastroRequest(
                "Usuario Teste",
                "teste@teste.edu.br",
                "senha12345",
                1L,
                tipoUsuario,
                true
        );
    }

    public static TokenConfirmacao criarTokenValido(Usuario usuario) {
        TokenConfirmacao token = new TokenConfirmacao();
        token.setId(1L);
        token.setToken("token-valido-uuid");
        token.setTipoToken(TipoToken.CONFIRMACAO_EMAIL);
        token.setExpiraEm(LocalDateTime.now().plusMinutes(5));
        token.setUsuario(usuario);
        return token;
    }

    public static TokenConfirmacao criarTokenExpirado(Usuario usuario) {
        TokenConfirmacao token = new TokenConfirmacao();
        token.setId(1L);
        token.setToken("token-expirado-uuid");
        token.setTipoToken(TipoToken.CONFIRMACAO_EMAIL);
        token.setExpiraEm(LocalDateTime.now().minusMinutes(10));
        token.setUsuario(usuario);
        return token;
    }

    public static Grupo criarGrupo(Faculdade faculdade, StatusGrupo status) {
        Grupo grupo = new Grupo();
        grupo.setId(1L);
        grupo.setNome("Grupo Teste");
        grupo.setCodigoConvite("ABCD2345");
        grupo.setCodigoConviteProfessor("WXYZ6789");
        grupo.setFaculdade(faculdade);
        grupo.setStatus(status);
        return grupo;
    }

    public static GrupoProfessor criarGrupoProfessor(Grupo grupo, Usuario professor, RoleGrupo role) {
        GrupoProfessor gp = new GrupoProfessor();
        gp.setId(1L);
        gp.setGrupo(grupo);
        gp.setProfessor(professor);
        gp.setRole(role);
        return gp;
    }

    public static GrupoMembro criarGrupoMembro(Grupo grupo, Usuario aluno, StatusMembro status) {
        GrupoMembro gm = new GrupoMembro();
        gm.setId(1L);
        gm.setGrupo(grupo);
        gm.setAluno(aluno);
        gm.setStatus(status);
        return gm;
    }

    public static Enquete criarEnquete(Grupo grupo) {
        Enquete enquete = new Enquete();
        enquete.setId(1L);
        enquete.setGrupo(grupo);
        enquete.setStatus(StatusEnquete.ATIVA);
        return enquete;
    }
}
