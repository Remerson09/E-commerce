package pweii.aula_10_09.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("J")
@NoArgsConstructor

@EqualsAndHashCode(callSuper = true)
public class PessoaJuridica extends Pessoa {

    @NotBlank(message = "O CNPJ é obrigatório.")
    @Size(max = 18, message = "O CNPJ não pode ter mais que 18 caracteres (incluindo formatação).")
    @Pattern(regexp = "^\\\\d{2}\\\\.\\\\d{3}\\\\.\\\\d{3}/\\\\d{4}-\\\\d{2}$|^\\\\d{14}$", message = "CNPJ inválido. Use o formato XX.XXX.XXX/YYYY-ZZ ou apenas 14 dígitos.")
    @Column(nullable = true, unique = true, length = 18)
    private String cnpj;

    @Column(nullable = true)
    private String razaoSocial;

    @Column(nullable = true)
    private String nome;

    @Column(nullable = true, unique = true)
    private String cpf;
}