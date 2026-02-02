package pweii.aula_10_09.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pweii.aula_10_09.model.entity.Usuario;

public interface UsuarioRepository  extends JpaRepository<Usuario, Long> {

    @Query("select u from Usuario u where u.login = ?1")
    Usuario usuario(String login);

    Usuario findByLogin(String login);
}
