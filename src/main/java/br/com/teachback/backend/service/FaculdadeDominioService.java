package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.FaculdadeDominioRequest;
import br.com.teachback.backend.dto.response.FaculdadeDominioResponse;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.mapper.FaculdadeDominioMapper;
import br.com.teachback.backend.model.Faculdade;
import br.com.teachback.backend.model.FaculdadeDominio;
import br.com.teachback.backend.model.Role;
import br.com.teachback.backend.model.Usuario;
import br.com.teachback.backend.repositories.FaculdadeDominioRepository;
import br.com.teachback.backend.repositories.FaculdadeRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FaculdadeDominioService {

    private final FaculdadeDominioRepository faculdadeDominioRepository;
    private final FaculdadeRepository faculdadeRepository;

    public FaculdadeDominioService(FaculdadeDominioRepository faculdadeDominioRepository, FaculdadeRepository faculdadeRepository) {
        this.faculdadeDominioRepository = faculdadeDominioRepository;
        this.faculdadeRepository = faculdadeRepository;
    }


    @Transactional
    public FaculdadeDominioResponse cadastrar(Long faculdadeId, FaculdadeDominioRequest request){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Faculdade faculdade = faculdadeRepository.findById(faculdadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));
        if(usuarioLogado.getRole() == Role.MODERADOR_CHEFE && !usuarioLogado.getFaculdade().getId().equals(faculdadeId)){
            throw new RegraDeNegocioException("Você só pode cadastrar domínios para a sua própria faculdade");
        }
        if(faculdadeDominioRepository.findByDominioAndFaculdade(request.dominio(), faculdade).isPresent()){
            throw new RegraDeNegocioException("Esse domínio já está cadastrado para essa faculdade");
        }
        FaculdadeDominio faculdadeDominio = FaculdadeDominioMapper.toEntity(request, faculdade);
        return FaculdadeDominioMapper.toDto(faculdadeDominioRepository.save(faculdadeDominio));
    }

    public List<FaculdadeDominioResponse> listar(Long faculdadeId){
        Faculdade faculdade = faculdadeRepository.findById(faculdadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Faculdade não encontrada"));
        List<FaculdadeDominio> faculdadeDominios = faculdadeDominioRepository.findByFaculdade(faculdade);
        return faculdadeDominios.stream().map(FaculdadeDominioMapper::toDto).toList();
    }

    @Transactional
    public void excluir(Long id){
        var usuarioLogado = (Usuario) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        FaculdadeDominio faculdadeDominio = faculdadeDominioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Dominio não encontrado"));
        if(usuarioLogado.getRole() == Role.MODERADOR_CHEFE && !usuarioLogado.getFaculdade().getId().equals(faculdadeDominio.getFaculdade().getId())){
            throw new RegraDeNegocioException("Você só pode excluir domínios da sua própria faculdade");
        }
        faculdadeDominioRepository.delete(faculdadeDominio);
    }
}
