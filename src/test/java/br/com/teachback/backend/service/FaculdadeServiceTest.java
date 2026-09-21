package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import br.com.teachback.backend.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaculdadeServiceTest {

    @Mock
    private FaculdadeRepository faculdadeRepository;

    @InjectMocks
    private FaculdadeService faculdadeService;

    @Test
    @DisplayName("Cadastro de faculdade com sucesso")
    void cadastrarComSucessoTest() {
        FaculdadeRequest request = new FaculdadeRequest("Universidade Teste", "UT", "02BN");
        Faculdade faculdadeSalva = TestDataFactory.criarFaculdade();

        when(faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla())).thenReturn(Optional.empty());
        when(faculdadeRepository.save(any(Faculdade.class))).thenReturn(faculdadeSalva);

        FaculdadeResponse response = faculdadeService.cadastrar(request);

        assertThat(response.id()).isEqualTo(faculdadeSalva.getId());
        assertThat(response.nome()).isEqualTo(faculdadeSalva.getNome());
        verify(faculdadeRepository).save(any(Faculdade.class));
    }

    @Test
    @DisplayName("Cadastro rejeitado: nome já existe")
    void cadastrarNomeDuplicadoTest() {
        FaculdadeRequest request = new FaculdadeRequest("Universidade Teste", "UT", "02BN");
        Faculdade faculdadeExistente = TestDataFactory.criarFaculdade();

        when(faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla())).thenReturn(Optional.of(faculdadeExistente));

        assertThrows(RegraDeNegocioException.class, () -> faculdadeService.cadastrar(request));
        verify(faculdadeRepository, never()).save(any(Faculdade.class));
    }

    @Test
    @DisplayName("Atualização com sucesso")
    void atualizarComSucessoTest() {
        Faculdade faculdadeExistente = TestDataFactory.criarFaculdade();
        FaculdadeRequest request = new FaculdadeRequest("Nome Atualizado", "NA", "03BN");

        when(faculdadeRepository.findById(faculdadeExistente.getId())).thenReturn(Optional.of(faculdadeExistente));
        when(faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla())).thenReturn(Optional.empty());

        FaculdadeResponse response = faculdadeService.atualizar(faculdadeExistente.getId(), request);

        assertThat(response.nome()).isEqualTo("Nome Atualizado");
        assertThat(response.sigla()).isEqualTo("NA");
    }

    @Test
    @DisplayName("Atualização rejeitada: nome e sigla já pertencem a outra faculdade")
    void atualizarNomeSiglaDuplicadosTest() {
        Faculdade faculdadeSendoEditada = TestDataFactory.criarFaculdade();
        Faculdade outraFaculdade = TestDataFactory.criarFaculdade();
        outraFaculdade.setId(999L);

        FaculdadeRequest request = new FaculdadeRequest("Nome Duplicado", "ND", "03BN");

        when(faculdadeRepository.findById(faculdadeSendoEditada.getId())).thenReturn(Optional.of(faculdadeSendoEditada));
        when(faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla())).thenReturn(Optional.of(outraFaculdade));

        assertThrows(RegraDeNegocioException.class, () -> faculdadeService.atualizar(faculdadeSendoEditada.getId(), request));
    }

    @Test
    @DisplayName("Atualização rejeitada: faculdade não encontrada")
    void atualizarFaculdadeNaoEncontradaTest() {
        FaculdadeRequest request = new FaculdadeRequest("Nome Atualizado", "NA", "03BN");

        when(faculdadeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> faculdadeService.atualizar(99L, request));
    }

    @Test
    @DisplayName("Listagem retorna todas as faculdades mapeadas")
    void listarTest() {
        Faculdade faculdade1 = TestDataFactory.criarFaculdade();
        Faculdade faculdade2 = TestDataFactory.criarFaculdade();

        when(faculdadeRepository.findAll()).thenReturn(List.of(faculdade1, faculdade2));

        List<FaculdadeResponse> response = faculdadeService.listar();

        assertThat(response).hasSize(2);
    }
}