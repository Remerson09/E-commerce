package pweii.aula_10_09.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pweii.aula_10_09.model.entity.Produto;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();
    // Todos os métodos (findAll, save, findById, deleteById) são herdados automaticamente.
}