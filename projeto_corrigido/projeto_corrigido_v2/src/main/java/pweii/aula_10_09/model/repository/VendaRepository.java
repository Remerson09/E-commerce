package pweii.aula_10_09.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pweii.aula_10_09.model.entity.Venda;

import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("select v from Venda v where v.dataVenda >= :dataInicial and v.dataVenda <= :dataFinal")
    List<Venda> findByDataVendaBetween(@Param("dataInicial") LocalDateTime dataInicial, @Param("dataFinal") LocalDateTime dataFinal);

    @Query("select v from Venda v where v.cliente.id = :clienteId and v.dataVenda >= :dataInicial and v.dataVenda <= :dataFinal")
    List<Venda> findByClienteIdAndDataVendaBetween(@Param("clienteId") Long clienteId, @Param("dataInicial") LocalDateTime dataInicial, @Param("dataFinal") LocalDateTime dataFinal);
    List<Venda> findByClienteId(Long clienteId);
    List<Venda> findByCliente(pweii.aula_10_09.model.entity.Pessoa cliente);
}