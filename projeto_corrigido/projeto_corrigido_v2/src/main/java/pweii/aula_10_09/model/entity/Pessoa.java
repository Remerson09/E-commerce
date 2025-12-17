package pweii.aula_10_09.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Classe Base para herança de PessoaFisica e PessoaJuridica
 * Estratégia: SINGLE_TABLE. Todos os campos em uma única tabela 'pessoas'.
 */
@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    @Column(length = 255)
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(min = 8, max = 20, message = "O telefone deve ter entre 8 e 20 caracteres")
    @Column(length = 20)
    private String telefone;

    // Associação: Um Cliente (Pessoa) pode ter Várias Vendas (OneToMany)
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Venda> vendas;


    public Pessoa(String email, String telefone) {
        this.email = email;
        this.telefone = telefone;

    }
}