package com.monitorellas.service;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.model.Usuario;
import com.monitorellas.repository.DispositivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispositivoService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    public Dispositivo criar(Dispositivo dispositivo, Usuario usuario) {
        dispositivo.setUsuario(usuario);
        return dispositivoRepository.save(dispositivo);
    }

    public List<Dispositivo> listarPorUsuario(Usuario usuario) {
        return dispositivoRepository.findByUsuario(usuario);
    }

    public Dispositivo buscarPorId(String id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    public Dispositivo buscarPorDeviceToken(String deviceToken) {
        return dispositivoRepository.findByDeviceToken(deviceToken)
                .orElse(null);
    }

    public Dispositivo buscarPorDeviceTokenEUsuario(String deviceToken, Usuario usuario) {
        return dispositivoRepository.findByDeviceTokenAndUsuario(deviceToken, usuario)
                .orElse(null);
    }

    public Dispositivo atualizar(String id, Dispositivo dispositivoAtualizado) {
        Dispositivo dispositivo = buscarPorId(id);
        
        // Bloquear alteração direta do status via REST
        if (dispositivoAtualizado.getNome() != null) {
            dispositivo.setNome(dispositivoAtualizado.getNome());
        }
        if (dispositivoAtualizado.getDeviceToken() != null) {
            dispositivo.setDeviceToken(dispositivoAtualizado.getDeviceToken());
        }
        if (dispositivoAtualizado.getSetor() != null) {
            dispositivo.setSetor(dispositivoAtualizado.getSetor());
        }
        if (dispositivoAtualizado.getOperacao() != null) {
            dispositivo.setOperacao(dispositivoAtualizado.getOperacao());
        }
        
        return dispositivoRepository.save(dispositivo);
    }

    public void deletar(String id) {
        Dispositivo dispositivo = buscarPorId(id);
        dispositivoRepository.delete(dispositivo);
    }

    public Dispositivo salvar(Dispositivo dispositivo) {
        return dispositivoRepository.save(dispositivo);
    }
}
