package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.FaculdadeDominioRequest;
import br.com.teachback.backend.dto.response.FaculdadeDominioResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.FaculdadeDominioRepository;
import br.com.teachback.backend.repository.FaculdadeRepository;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaculdadeDominioServiceTest {

    @Mock
    private FaculdadeDominioRepository faculdadeDominioRepository;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    @InjectMocks
    private FaculdadeDominioService faculdadeDominioService;

    private MockedStatic<SecurityContextHolder> mockLoggedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);
        mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        return mockedStatic;
    }

    private Faculdade outraFaculdade() {
        Faculdade outra = TestDataFactory.criarFaculdade();
        outra.setId(2L);
        return outra;
    }

    private Usuario admin() {
        Usuario admin = TestDataFactory.criarUsuario(Role.ADMIN, StatusUsuario.ATIVO);
        admin.setFaculdade(null);
        return admin;
    }

    // ---------- listar ----------

    @Test
    @DisplayName("Listar: chefe lista os domínios da própria faculdade")
    void listarChefeNaPropriaFaculdadeTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Faculdade faculdade = chefe.getFaculdade();
        FaculdadeDominio dominio = TestDataFactory.criarFaculdadeDominio(faculdade);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeRepository.findById(faculdade.getId())).thenReturn(Optional.of(faculdade));
            when(faculdadeDominioRepository.findByFaculdade(faculdade)).thenReturn(List.of(dominio));

            List<FaculdadeDominioResponse> response = faculdadeDominioService.listar(faculdade.getId());

            assertThat(response).hasSize(1);
        }
    }

    @Test
    @DisplayName("Listar rejeitado: chefe tenta listar os domínios de outra faculdade")
    void listarChefeEmOutraFaculdadeTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Faculdade outra = outraFaculdade();

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeRepository.findById(outra.getId())).thenReturn(Optional.of(outra));

            assertThrows(RegraDeNegocioException.class, () -> faculdadeDominioService.listar(outra.getId()));
            verify(faculdadeDominioRepository, never()).findByFaculdade(any());
        }
    }

    @Test
    @DisplayName("Listar: admin lista os domínios de qualquer faculdade (faculdade do admin é null)")
    void listarAdminTest() {
        Usuario admin = admin();
        Faculdade faculdade = TestDataFactory.criarFaculdade();

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(admin)) {
            when(faculdadeRepository.findById(faculdade.getId())).thenReturn(Optional.of(faculdade));
            when(faculdadeDominioRepository.findByFaculdade(faculdade)).thenReturn(List.of());

            assertThat(faculdadeDominioService.listar(faculdade.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Listar rejeitado: faculdade não existe")
    void listarFaculdadeInexistenteTest() {
        Usuario admin = admin();

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(admin)) {
            when(faculdadeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> faculdadeDominioService.listar(999L));
        }
    }

    // ---------- cadastrar ----------

    @Test
    @DisplayName("Cadastrar: admin cadastra domínio em qualquer faculdade")
    void cadastrarAdminTest() {
        Usuario admin = admin();
        Faculdade faculdade = TestDataFactory.criarFaculdade();
        FaculdadeDominioRequest request = new FaculdadeDominioRequest("novo.edu.br");

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(admin)) {
            when(faculdadeRepository.findById(faculdade.getId())).thenReturn(Optional.of(faculdade));
            when(faculdadeDominioRepository.findByDominioAndFaculdade("novo.edu.br", faculdade)).thenReturn(Optional.empty());
            when(faculdadeDominioRepository.save(any(FaculdadeDominio.class))).thenAnswer(inv -> inv.getArgument(0));

            FaculdadeDominioResponse response = faculdadeDominioService.cadastrar(faculdade.getId(), request);

            assertThat(response.dominio()).isEqualTo("novo.edu.br");
            verify(faculdadeDominioRepository).save(any(FaculdadeDominio.class));
        }
    }

    @Test
    @DisplayName("Cadastrar rejeitado: chefe tenta cadastrar domínio em outra faculdade")
    void cadastrarChefeEmOutraFaculdadeTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Faculdade outra = outraFaculdade();

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeRepository.findById(outra.getId())).thenReturn(Optional.of(outra));

            assertThrows(RegraDeNegocioException.class,
                    () -> faculdadeDominioService.cadastrar(outra.getId(), new FaculdadeDominioRequest("novo.edu.br")));
            verify(faculdadeDominioRepository, never()).save(any(FaculdadeDominio.class));
        }
    }

    @Test
    @DisplayName("Cadastrar rejeitado: domínio já existe nessa faculdade")
    void cadastrarDominioDuplicadoTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        Faculdade faculdade = chefe.getFaculdade();
        FaculdadeDominio existente = TestDataFactory.criarFaculdadeDominio(faculdade);

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeRepository.findById(faculdade.getId())).thenReturn(Optional.of(faculdade));
            when(faculdadeDominioRepository.findByDominioAndFaculdade(existente.getDominio(), faculdade))
                    .thenReturn(Optional.of(existente));

            assertThrows(RegraDeNegocioException.class,
                    () -> faculdadeDominioService.cadastrar(faculdade.getId(), new FaculdadeDominioRequest(existente.getDominio())));
            verify(faculdadeDominioRepository, never()).save(any(FaculdadeDominio.class));
        }
    }

    // ---------- excluir ----------

    @Test
    @DisplayName("Excluir: chefe exclui domínio da própria faculdade")
    void excluirChefeNaPropriaFaculdadeTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        FaculdadeDominio dominio = TestDataFactory.criarFaculdadeDominio(chefe.getFaculdade());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeDominioRepository.findById(dominio.getId())).thenReturn(Optional.of(dominio));

            faculdadeDominioService.excluir(dominio.getId());

            verify(faculdadeDominioRepository).delete(dominio);
        }
    }

    @Test
    @DisplayName("Excluir rejeitado: chefe tenta excluir domínio de outra faculdade")
    void excluirChefeEmOutraFaculdadeTest() {
        Usuario chefe = TestDataFactory.criarUsuario(Role.MODERADOR_CHEFE, StatusUsuario.ATIVO);
        FaculdadeDominio dominioDeOutra = TestDataFactory.criarFaculdadeDominio(outraFaculdade());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(chefe)) {
            when(faculdadeDominioRepository.findById(dominioDeOutra.getId())).thenReturn(Optional.of(dominioDeOutra));

            assertThrows(RegraDeNegocioException.class, () -> faculdadeDominioService.excluir(dominioDeOutra.getId()));
            verify(faculdadeDominioRepository, never()).delete(any(FaculdadeDominio.class));
        }
    }

    @Test
    @DisplayName("Excluir: admin exclui domínio de qualquer faculdade")
    void excluirAdminTest() {
        Usuario admin = admin();
        FaculdadeDominio dominio = TestDataFactory.criarFaculdadeDominio(outraFaculdade());

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(admin)) {
            when(faculdadeDominioRepository.findById(dominio.getId())).thenReturn(Optional.of(dominio));

            faculdadeDominioService.excluir(dominio.getId());

            verify(faculdadeDominioRepository).delete(dominio);
        }
    }

    @Test
    @DisplayName("Excluir rejeitado: domínio não existe")
    void excluirDominioInexistenteTest() {
        Usuario admin = admin();

        try (MockedStatic<SecurityContextHolder> ignored = mockLoggedUser(admin)) {
            when(faculdadeDominioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RecursoNaoEncontradoException.class, () -> faculdadeDominioService.excluir(999L));
        }
    }
}