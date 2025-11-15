package com.monitorellas.service;

import com.monitorellas.model.Operacao;
import com.monitorellas.model.Usuario;
import com.monitorellas.repository.OperacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperacaoService {

    @Autowired
    private OperacaoRepository operacaoRepository;

    public Operacao criar(Operacao operacao, Usuario usuario) {
        operacao.setUsuario(usuario);
        return operacaoRepository.save(operacao);
    }

    public List<Operacao> listarPorUsuario(Usuario usuario) {
        return operacaoRepository.findByUsuarioAndAtivoOrderByNomeAsc(usuario, true);
    }

    public Operacao buscarPorId(String id) {
        return operacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operação não encontrada"));
    }

    public Operacao atualizar(String id, Operacao operacaoAtualizada) {
        Operacao operacao = buscarPorId(id);
        
        if (operacaoAtualizada.getNome() != null) {
            operacao.setNome(operacaoAtualizada.getNome());
        }
        if (operacaoAtualizada.getMetaDiaria() != null) {
            operacao.setMetaDiaria(operacaoAtualizada.getMetaDiaria());
        }
        if (operacaoAtualizada.getSetor() != null) {
            operacao.setSetor(operacaoAtualizada.getSetor());
        }
        if (operacaoAtualizada.getDescricao() != null) {
            operacao.setDescricao(operacaoAtualizada.getDescricao());
        }
        if (operacaoAtualizada.getAtivo() != null) {
            operacao.setAtivo(operacaoAtualizada.getAtivo());
        }
        
        return operacaoRepository.save(operacao);
    }

    public void desativar(String id) {
        Operacao operacao = buscarPorId(id);
        operacao.setAtivo(false);
        operacaoRepository.save(operacao);
    }
}
