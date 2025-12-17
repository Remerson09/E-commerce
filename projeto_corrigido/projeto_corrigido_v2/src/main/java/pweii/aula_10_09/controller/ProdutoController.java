package pweii.aula_10_09.controller;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pweii.aula_10_09.model.entity.Produto;
import pweii.aula_10_09.model.entity.Venda;
import pweii.aula_10_09.model.entity.ItemVenda;
import pweii.aula_10_09.model.repository.ProdutoRepository;

import java.util.ArrayList;

import java.util.Optional;

@Transactional
@Controller
@RequestMapping("/produto")
@SessionAttributes("vendaEmAndamento")
public class ProdutoController {

    @Autowired
    ProdutoRepository produtoRepository;

    /**
     * ESSENCIAL: Cria a venda na sessão se ela não existir.
     * Substitui a necessidade de "if (venda == null)" dentro dos métodos.
     */
    @ModelAttribute("vendaEmAndamento")
    public Venda inicializarVenda() {
        Venda v = new Venda();
        v.setItens(new ArrayList<>());
        return v;
    }
    @GetMapping("/list")
    public ModelAndView listar(ModelMap model,
                               @ModelAttribute("vendaEmAndamento") Venda vendaAtual) {
        model.addAttribute("produtos", produtoRepository.findAll());

        // Soma simples das quantidades para o ícone do carrinho
        int carrinhoCount = vendaAtual.getItens().stream()
                .mapToInt(item -> item.getQuantidade().intValue()).sum();

        model.addAttribute("carrinhoCount", carrinhoCount);
        return new ModelAndView("produto/list", model);
    }
    @PostMapping("/adicionar/{id}")
    public ModelAndView adicionarAoCarrinho(@PathVariable("id") Long produtoId,
                                            @RequestParam("quantidade") int quantidade,
                                            @ModelAttribute("vendaEmAndamento") Venda vendaAtual,
                                            RedirectAttributes attr) {

        Produto produto = produtoRepository.findById(produtoId).orElse(null);

        if (produto == null || quantidade <= 0) {
            attr.addFlashAttribute("error", "Produto inválido ou quantidade zero.");
            return new ModelAndView("redirect:/produto/list");
        }

        // Busca se o produto já está na lista da venda
        Optional<ItemVenda> itemExistente = vendaAtual.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Se já existe, apenas aumenta a quantidade
            itemExistente.get().setQuantidade(itemExistente.get().getQuantidade() + quantidade);
        } else {
            // Se é novo, cria o ItemVenda e adiciona na lista da vendaAtual
            ItemVenda novoItem = new ItemVenda();
            novoItem.setProduto(produto);
            novoItem.setQuantidade((double) quantidade);
            novoItem.setVenda(vendaAtual);
            vendaAtual.getItens().add(novoItem);
        }

        attr.addFlashAttribute("success", produto.getDescricao() + " adicionado!");
        return new ModelAndView("redirect:/produto/list");
    }

    // --- CRUD BÁSICO ---
    @GetMapping("/form")
    public String form(Produto produto) {
        return "produto/form";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("produto", produtoRepository.findById(id).orElse(null));
        return new ModelAndView("produto/form", model);
    }

    @PostMapping({"/save", "/update"})
    public String save(Produto produto, RedirectAttributes attr) {
        produtoRepository.save(produto);
        attr.addFlashAttribute("success", "Operação realizada com sucesso!");
        return "redirect:/produto/list";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id, RedirectAttributes attr) {
        produtoRepository.deleteById(id);
        attr.addFlashAttribute("success", "Produto removido!");
        return "redirect:/produto/list";
    }
}