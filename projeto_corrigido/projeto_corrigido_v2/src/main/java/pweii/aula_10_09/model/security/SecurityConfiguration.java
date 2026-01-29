package pweii.aula_10_09.model.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    UsuarioDetailsConfig usuarioDetailsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(customizer -> customizer
                        // 1. Recursos estáticos e login
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()

                        // 2. Cadastro de pessoa física e jurídica aberto para novos usuários
                        .requestMatchers("/pessoa/formPF", "/pessoa/formPJ", "/pessoa/savePF", "/pessoa/savePJ").permitAll()

                        // 3. Visualização de produtos e carrinho pública
                        .requestMatchers("/", "/produto/list", "/vendas/carrinho", "/produto/adicionar/**", "/vendas/removerItem/**").permitAll()

                        // 4. Ações restritas ao ADMIN
                        .requestMatchers("/pessoa/list", "/pessoa/remove/**", "/pessoa/edit/**", "/produto/form", "/produto/edit/**", "/produto/remove/**", "/produto/save", "/produto/update").hasRole("ADMIN")

                        // 5. Finalizar venda e ver histórico exige login
                        .requestMatchers("/vendas/finalizar", "/vendas/list", "/vendas/detalhe/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(customizer -> customizer
                        .loginPage("/login")
                        .defaultSuccessUrl("/produto/list", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/produto/list")
                        .permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // Para o H2 console
                .rememberMe(withDefaults());

        return http.build();
    }

    @Autowired
    public void configureUserDetails(final AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(usuarioDetailsConfig).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
