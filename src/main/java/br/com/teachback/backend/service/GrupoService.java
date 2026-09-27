package br.com.teachback.backend.service;

import br.com.teachback.backend.dto.request.CodigoConviteRequest;
import br.com.teachback.backend.dto.request.GrupoRequest;
import br.com.teachback.backend.dto.response.GrupoProfessorResponse;
import br.com.teachback.backend.dto.response.GrupoResponse;
import br.com.teachback.backend.exception.AcessoNegadoException;
import br.com.teachback.backend.exception.RecursoNaoEncontradoException;
import br.com.teachback.backend.exception.RegraDeNegocioException;
import br.com.teachback.backend.mapper.GrupoMapper;
import br.com.teachback.backend.model.*;
import br.com.teachback.backend.repository.EnqueteRepository;
import br.com.teachback.backend.repository.GrupoMembroRepository;
import br.com.teachback.backend.repository.GrupoProfessorRepository;
import br.com.teachback.backend.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 8;

    private final GrupoRepository grupoRepository;
    private final GrupoProfessorRepository grupoProfessorRepository;
    private final GrupoMembroRepository grupoMembroRepository;
    private final EnqueteRepository enqueteRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public GrupoProfessorResponse criar(GrupoRequest request) {
        Usuario criador = usuarioLogado();

        Grupo grupo = GrupoMapper.toEntity(request, criador.getFaculdade());
        grupo.setCodigoConvite(gerarCodigoUnico(grupoRepository::existsByCodigoConvite));
        grupo.setCodigoConviteProfessor(gerarCodigoUnico(grupoRepository::existsByCodigoConviteProfessor));
        grupo = grupoRepository.save(grupo);

        GrupoProfessor grupoProfessor = new GrupoProfessor();
        grupoProfessor.setGrupo(grupo);
        grupoProfessor.setProfessor(criador);
        grupoProfessor.setRole(RoleGrupo.CRIADOR);
        grupoProfessor = grupoProfessorRepository.save(grupoProfessor);

        return GrupoMapper.toDTO(grupoProfessor);
    }

    @Transactional
    public GrupoProfessorResponse atualizar(Long id, GrupoRequest request) {
        GrupoProfessor grupoProfessor = buscarGrupoProfessorAtivo(id, usuarioLogado());

        Grupo grupo = grupoProfessor.getGrupo();
        GrupoMapper.atualizar(grupo, request);
        grupoRepository.save(grupo);

        return GrupoMapper.toDTO(grupoProfessor);
    }

    @Transactional
    public GrupoResponse entrarComoAluno(CodigoConviteRequest request) {
        Usuario aluno = usuarioLogado();

        Grupo grupo = grupoRepository.findByCodigoConviteAndStatus(request.codigo(), StatusGrupo.ATIVO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Código de convite inválido."));

        if (grupoMembroRepository.existsByGrupoAndAluno(grupo, aluno)) {
            throw new RegraDeNegocioException("Você já faz parte deste grupo.");
        }

        GrupoMembro membro = new GrupoMembro();
        membro.setGrupo(grupo);
        membro.setAluno(aluno);
        membro.setStatus(StatusMembro.ATIVO);
        grupoMembroRepository.save(membro);

        return GrupoMapper.toDTO(grupo);
    }

    @Transactional
    public GrupoProfessorResponse entrarComoProfessor(CodigoConviteRequest request) {
        Usuario professor = usuarioLogado();

        Grupo grupo = grupoRepository.findByCodigoConviteProfessorAndStatus(request.codigo(), StatusGrupo.ATIVO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Código de convite inválido."));

        if (grupoProfessorRepository.existsByGrupoAndProfessor(grupo, professor)) {
            throw new RegraDeNegocioException("Você já faz parte deste grupo.");
        }

        GrupoProfessor grupoProfessor = new GrupoProfessor();
        grupoProfessor.setGrupo(grupo);
        grupoProfessor.setProfessor(professor);
        grupoProfessor.setRole(RoleGrupo.CO_DOCENTE);
        grupoProfessor = grupoProfessorRepository.save(grupoProfessor);

        return GrupoMapper.toDTO(grupoProfessor);
    }

    @Transactional
    public void encerrar(Long id) {
        GrupoProfessor grupoProfessor = buscarGrupoProfessorAtivo(id, usuarioLogado());

        if (grupoProfessor.getRole() != RoleGrupo.CRIADOR) {
            throw new AcessoNegadoException("Apenas o professor criador pode encerrar o grupo.");
        }

        Grupo grupo = grupoProfessor.getGrupo();
        grupo.setStatus(StatusGrupo.ENCERRADO);
        grupoRepository.save(grupo);

        enqueteRepository.findByGrupoAndStatus(grupo, StatusEnquete.ATIVA)
                .ifPresent(enquete -> {
                    enquete.setStatus(StatusEnquete.ENCERRADA);
                    enqueteRepository.save(enquete);
                });
    }

    @Transactional(readOnly = true)
    public Page<GrupoProfessorResponse> listarGruposDoProfessor(Pageable pageable) {
        Usuario professor = usuarioLogado();
        return grupoProfessorRepository
                .findByProfessorAndGrupo_Status(professor, StatusGrupo.ATIVO, pageable)
                .map(GrupoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<GrupoResponse> listarGruposDoAluno(Pageable pageable) {
        Usuario aluno = usuarioLogado();
        return grupoMembroRepository
                .findByAlunoAndStatusAndGrupo_Status(aluno, StatusMembro.ATIVO, StatusGrupo.ATIVO, pageable)
                .map(gm -> GrupoMapper.toDTO(gm.getGrupo()));
    }

    private GrupoProfessor buscarGrupoProfessorAtivo(Long grupoId, Usuario professor) {
        GrupoProfessor grupoProfessor = grupoProfessorRepository.findByGrupo_IdAndProfessor(grupoId, professor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado."));

        if (grupoProfessor.getGrupo().getStatus() != StatusGrupo.ATIVO) {
            throw new RegraDeNegocioException("Este grupo está encerrado.");
        }

        return grupoProfessor;
    }

    private String gerarCodigoUnico(Predicate<String> jaExiste) {
        String codigo;
        do {
            codigo = gerarCodigo();
        } while (jaExiste.test(codigo));
        return codigo;
    }

    private String gerarCodigo() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(ALFABETO_CODIGO.charAt(random.nextInt(ALFABETO_CODIGO.length())));
        }
        return sb.toString();
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
