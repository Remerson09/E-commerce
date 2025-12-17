package pweii.aula_10_09.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pweii.aula_10_09.model.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Todos os métodos (findAll, save, findById, deleteById) são herdados automaticamente.
}