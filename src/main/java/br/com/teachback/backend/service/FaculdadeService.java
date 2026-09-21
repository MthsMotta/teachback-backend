package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.FaculdadeRequest;
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
        if (faculdadeRepository.findByNome(request.nome()).isPresent()){
            throw new RegraDeNegocioException("Já existe uma faculdade com esse nome.");
        }
        Faculdade faculdade = FaculdadeMapper.toEntity(request);
        return FaculdadeMapper.toDTO(faculdadeRepository.save(faculdade));
    }

    @Transactional
    public FaculdadeResponse atualizar(Long id, FaculdadeRequest request) {
        Faculdade faculdade = faculdadeRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));
        FaculdadeMapper.updateEntityFromDTO(request, faculdade);
        return FaculdadeMapper.toDTO(faculdade);
    }

    public List<FaculdadeResponse> listar() {
        List<Faculdade> faculdades = faculdadeRepository.findAll();
        return faculdades.stream().map(FaculdadeMapper::toDTO).toList();
    }
}
