package com.monitorellas.service;

import com.monitorellas.model.Funcionario;
import com.monitorellas.repository.FuncionarioRepository;
import com.monitorellas.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public Funcionario criar(Funcionario funcionario) {
        return funcionarioRepository.save(funcionario);
    }


    public List<Funcionario> listarPorUsuario(Usuario usuario) {
        return funcionarioRepository.findByUsuario(usuario);
    }

    public Funcionario buscarPorId(String id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
    }


    public Funcionario buscarPorCodigoEUsuario(String codigo, Usuario usuario) {
        return funcionarioRepository.findByCodigoAndUsuario(codigo, usuario)
                .orElse(null);
    }

    public Funcionario atualizar(String id, Funcionario funcionarioAtualizado) {
        Funcionario funcionario = buscarPorId(id);
        
        if (funcionarioAtualizado.getNome() != null) {
            funcionario.setNome(funcionarioAtualizado.getNome());
        }
        if (funcionarioAtualizado.getCodigo() != null) {
            funcionario.setCodigo(funcionarioAtualizado.getCodigo());
        }
        if (funcionarioAtualizado.getFuncao() != null) {
            funcionario.setFuncao(funcionarioAtualizado.getFuncao());
        }
        if (funcionarioAtualizado.getAtivo() != null) {
            funcionario.setAtivo(funcionarioAtualizado.getAtivo());
        }
        
        return funcionarioRepository.save(funcionario);
    }

    public void deletar(String id) {
        Funcionario funcionario = buscarPorId(id);
        funcionarioRepository.delete(funcionario);
    }
}
