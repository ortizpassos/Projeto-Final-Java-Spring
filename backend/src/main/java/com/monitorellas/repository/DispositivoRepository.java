package com.monitorellas.repository;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoRepository extends MongoRepository<Dispositivo, String> {
    Optional<Dispositivo> findByDeviceToken(String deviceToken);
    List<Dispositivo> findByUsuario(Usuario usuario);
    List<Dispositivo> findByStatusAndUltimaAtualizacaoLessThan(String status, LocalDateTime dateTime);
    Optional<Dispositivo> findByDeviceTokenAndUsuario(String deviceToken, Usuario usuario);
}
