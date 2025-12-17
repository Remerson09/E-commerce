package pweii.aula_10_09.model.repository;// No PessoaRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pweii.aula_10_09.model.entity.Pessoa;

import java.util.List;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    // Este método espera que o parâmetro 'nome' já contenha os wildcards (%)
    @Query("select p from Pessoa p where p.nome like :nome or p.razaoSocial like :nome")
    List<Pessoa> findByNomeOrRazaoSocialContaining(@Param("nome") String nome);
}