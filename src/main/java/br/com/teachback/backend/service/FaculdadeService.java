package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.FaculdadeRequest;
import br.com.teachback.backend.dto.response.FaculdadeAutoCompleteResponse;
import br.com.teachback.backend.dto.response.FaculdadeResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.mapper.FaculdadeMapper;
import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaculdadeService {

    private final FaculdadeRepository faculdadeRepository;

    public FaculdadeService(FaculdadeRepository faculdadeRepository) {
        this.faculdadeRepository = faculdadeRepository;
    }

    public FaculdadeResponse cadastrar(FaculdadeRequest request) {
        if (faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla()).isPresent()){
            throw new RegraDeNegocioException("Já existe uma faculdade com esse nome e sigla.");
        }
        Faculdade faculdade = FaculdadeMapper.toEntity(request);
        return FaculdadeMapper.toDTO(faculdadeRepository.save(faculdade));
    }

    @Transactional
    public FaculdadeResponse atualizar(Long id, FaculdadeRequest request) {
        Faculdade faculdade = faculdadeRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));

        faculdadeRepository.findByNomeAndSigla(request.nome(), request.sigla())
                .filter(f -> !f.getId().equals(id))
                .ifPresent(f -> {
                    throw new RegraDeNegocioException("Já existe uma faculdade com esse nome e sigla.");
                });

        FaculdadeMapper.updateEntityFromDTO(request, faculdade);
        return FaculdadeMapper.toDTO(faculdade);
    }

    public List<FaculdadeResponse> listar() {
        List<Faculdade> faculdades = faculdadeRepository.findAll();
        return faculdades.stream().map(FaculdadeMapper::toDTO).toList();
    }

    public List<FaculdadeAutoCompleteResponse> listarPorNomeOuSigla(String termo) {
        List<Faculdade> faculdades = faculdadeRepository.findByNomeContainingIgnoreCaseOrSiglaContainingIgnoreCase(termo, termo);
        return faculdades.stream().map(FaculdadeMapper::autoCompleteToDTO).toList();
    }
}
