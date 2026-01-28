package pweii.aula_10_09.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal; // 💡 Importação essencial

@Entity
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "produtos")
public class Produto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 2, max = 100, message = "A descrição deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String descricao;

    @NotNull(message = "O valor do produto deve ser informado")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String imagemUrl;
    // Getters e Setters atualizados para BigDecimal
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}