package pweii.aula_10_09.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pweii.aula_10_09.model.entity.Pessoa;
import pweii.aula_10_09.model.entity.Usuario;

import java.util.List;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    // Este método espera que o parâmetro 'nome' já contenha os wildcards (%)
    @Query("select p from Pessoa p where p.nome like :nome or p.razaoSocial like :nome")
    List<Pessoa> findByNomeOrRazaoSocialContaining(@Param("nome") String nome);

    @Query("SELECT u.pessoa FROM Usuario u WHERE u = :usuario")
    Pessoa findByUsuario(@Param("usuario") Usuario usuario);
}