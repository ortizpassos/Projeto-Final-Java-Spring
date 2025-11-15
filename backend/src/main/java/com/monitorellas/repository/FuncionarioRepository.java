package com.monitorellas.repository;

import com.monitorellas.model.Funcionario;
import com.monitorellas.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends MongoRepository<Funcionario, String> {
    Optional<Funcionario> findByCodigo(String codigo);
    List<Funcionario> findByUsuario(Usuario usuario);
    Optional<Funcionario> findByCodigoAndUsuario(String codigo, Usuario usuario);
}
