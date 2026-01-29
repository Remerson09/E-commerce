package pweii.aula_10_09.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pweii.aula_10_09.model.entity.Produto;
import pweii.aula_10_09.model.entity.Role;
import pweii.aula_10_09.model.entity.Usuario;
import pweii.aula_10_09.model.repository.ProdutoRepository;
import pweii.aula_10_09.model.repository.RoleRepository;
import pweii.aula_10_09.model.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Criar Roles se não existirem
        Role roleAdmin = roleRepository.findByNome("ROLE_ADMIN");
        if (roleAdmin == null) {
            roleAdmin = new Role();
            roleAdmin.setNome("ROLE_ADMIN");
            roleRepository.save(roleAdmin);
        }

        Role roleUser = roleRepository.findByNome("ROLE_USER");
        if (roleUser == null) {
            roleUser = new Role();
            roleUser.setNome("ROLE_USER");
            roleRepository.save(roleUser);
        }

        // Criar Admin se não existir
        if (usuarioRepository.findByLogin("admin") == null) {
            Usuario admin = new Usuario();
            admin.setLogin("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRoles(Arrays.asList(roleAdmin, roleUser));
            usuarioRepository.save(admin);
        }

        // Criar João (Usuário de teste) se não existir
        if (usuarioRepository.findByLogin("joao") == null) {
            Usuario joao = new Usuario();
            joao.setLogin("joao");
            joao.setPassword(passwordEncoder.encode("123"));
            joao.setRoles(Arrays.asList(roleUser));
            usuarioRepository.save(joao);
        }

        // Adicionar alguns produtos para teste se a lista estiver vazia
        if (produtoRepository.count() == 0) {
            Produto p1 = new Produto();
            p1.setDescricao("Café Gourmet 500g");
            p1.setValor(new BigDecimal("25.90"));
            p1.setImagemUrl("https://images.tcdn.com.br/img/img_prod/1303842/cafe_torrado_e_moido_500gr_torra_media_77_2_dd6c41c3b45515dafc757784acce0c19.jpg");
            produtoRepository.save(p1);

            Produto p2 = new Produto();
            p2.setDescricao("Caneca Personalizada");
            p2.setValor(new BigDecimal("35.00"));
            p2.setImagemUrl("https://cdn.awsli.com.br/600x450/161/161611/produto/124116568/7f8c04586d.jpg");
            produtoRepository.save(p2);

            Produto p3 = new Produto();
            p3.setDescricao("Prensa Francesa");
            p3.setValor(new BigDecimal("89.90"));
            p3.setImagemUrl("https://m.media-amazon.com/images/I/61N8O2m7SXL._AC_SL1500_.jpg");
            produtoRepository.save(p3);
        }
    }
}
