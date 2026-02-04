package pweii.aula_10_09.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Pessoa cliente;

    @Column(nullable = false)
    private LocalDateTime dataVenda;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemVenda> itens = new ArrayList<>(); // Uma lista dos itens que compõem a venda. É inicializada com um ArrayList vazio para evitar NullPointerExceptions

    // Nome do método ajustado para getTotal() (UML). Mantido BigDecimal.
    @Transient
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;

        if (itens != null) {
            for (ItemVenda item : itens) {
                total = total.add(item.getTotal());
            }
        }
        return total;
    }
}