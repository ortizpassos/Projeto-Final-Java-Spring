package com.monitorellas.repository;

import com.monitorellas.model.Dispositivo;
import com.monitorellas.model.Funcionario;
import com.monitorellas.model.Operacao;
import com.monitorellas.model.Producao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProducaoRepository extends MongoRepository<Producao, String> {
    Optional<Producao> findByFuncionarioAndDispositivoAndOperacaoAndDataHoraBetween(
            Funcionario funcionario, 
            Dispositivo dispositivo, 
            Operacao operacao, 
            LocalDateTime start, 
            LocalDateTime end
    );
    
    Optional<Producao> findByFuncionarioAndDispositivoAndDataHoraBetween(
            Funcionario funcionario, 
            Dispositivo dispositivo, 
            LocalDateTime start, 
            LocalDateTime end
    );
}
