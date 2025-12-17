package pweii.aula_10_09.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "itens_venda")
public class ItemVenda implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos itens pertencem a uma única Venda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    // Muitos itens referenciam um único Produto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // 💡 CORREÇÃO: Usando Double para alinhar ao diagrama
    @Column(nullable = false)
    private Double quantidade;

    // Preço histórico no momento da venda
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    // 💡 CORREÇÃO: Nome do método ajustado para total() (UML)
    @Transient
    public BigDecimal getTotal() {
        if (precoUnitario == null || quantidade == null) {
            return BigDecimal.ZERO;
        }
        // Usamos doubleValue() para multiplicar, mas é mais seguro usar BigDecimal.valueOf() para evitar perda de precisão
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}