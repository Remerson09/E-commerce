package pweii.aula_10_09.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pweii.aula_10_09.model.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNome(String nome);
}
