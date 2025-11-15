package com.monitorellas.repository;

import com.monitorellas.model.Operacao;
import com.monitorellas.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacaoRepository extends MongoRepository<Operacao, String> {
    List<Operacao> findByUsuarioAndAtivoOrderByNomeAsc(Usuario usuario, Boolean ativo);
}
