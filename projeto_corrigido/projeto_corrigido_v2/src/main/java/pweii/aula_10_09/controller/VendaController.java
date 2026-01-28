package pweii.aula_10_09.controller;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pweii.aula_10_09.model.entity.Venda;
import pweii.aula_10_09.model.entity.ItemVenda;
import pweii.aula_10_09.model.repository.VendaRepository;
import pweii.aula_10_09.model.repository.PessoaRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Transactional
@Controller
@RequestMapping("/vendas")
@SessionAttributes("vendaEmAndamento")
public class VendaController {

    @Autowired
    VendaRepository vendaRepository;

    @Autowired
    PessoaRepository pessoaRepository;

    @GetMapping("/list")
    public ModelAndView listar(ModelMap model) {
        model.addAttribute("vendas", vendaRepository.findAll());
        return new ModelAndView("vendas/list", model);
    }


    @GetMapping("/carrinho")
    public ModelAndView verCarrinho(@ModelAttribute("vendaEmAndamento") Venda venda, ModelMap model) {

        // Cálculo manual do total percorrendo os itens do carrinho
        BigDecimal totalCalculado = venda.getItens().stream()
                .map(item -> item.getProduto().getValor().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("vendaAtual", venda);
        model.addAttribute("clientes", pessoaRepository.findAll());

        // Enviando o total calculado para a variável que o HTML espera
        model.addAttribute("totalVenda", totalCalculado);

        return new ModelAndView("vendas/carrinho", model);
    }
    @PostMapping("/finalizar")
    public String finalizar(@RequestParam("clienteId") Long clienteId,
                            @RequestParam("descricao") String descricao,
                            @ModelAttribute("vendaEmAndamento") Venda venda,
                            SessionStatus status,
                            RedirectAttributes attr) {

        if (venda.getItens().isEmpty()) {
            attr.addFlashAttribute("error", "Carrinho vazio!");
            return "redirect:/vendas/carrinho";
        }

        // 🔥 IMPORTANTE: Se o ID vier preenchido de um SQL antigo, o Hibernate dará erro.
        // Se a intenção é sempre uma venda nova, garantimos o ID nulo:
        venda.setId(null);

        venda.setDataVenda(LocalDateTime.now());
        venda.setDescricao(descricao);
        venda.setCliente(pessoaRepository.findById(clienteId).orElse(null));

        for (ItemVenda item : venda.getItens()) {
            item.setVenda(venda);
            item.setPrecoUnitario(item.getProduto().getValor());
            item.setId(null); // Garante que os itens também sejam inserções novas
        }

        vendaRepository.save(venda);
        status.setComplete(); // Limpa a @SessionAttributes

        attr.addFlashAttribute("success", "Venda finalizada com sucesso!");
        return "redirect:/vendas/list";
    }

    @GetMapping("/removerItem/{id}")
    public String removerItem(@PathVariable("id") Long id,
                              @ModelAttribute("vendaEmAndamento") Venda venda) {

        venda.getItens().removeIf(i -> i.getProduto().getId().equals(id));
        return "redirect:/vendas/carrinho";
    }

    @GetMapping("/detalhe/{id}")
    public ModelAndView details(@PathVariable("id") Long id, ModelMap model) {

        //Busca a venda no banco pelo ID
        Optional<Venda> busca = vendaRepository.findById(id);

        //Se encontrou, coloca no balde. Se não, coloca uma venda vazia.
        if (busca.isPresent()) {
            model.addAttribute("venda", busca.get());
        } else {
            model.addAttribute("venda", new Venda());
        }

        //Abre a página de detalhes
        return new ModelAndView("vendas/detalhe", model);
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id) {
        vendaRepository.deleteById(id);
        return "redirect:/vendas/list";
    }
}