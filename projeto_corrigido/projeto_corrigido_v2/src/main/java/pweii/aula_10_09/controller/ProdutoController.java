package pweii.aula_10_09.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid; // 💡 Importante para a validação
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult; // 💡 Importante para capturar erros
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

        Optional<ItemVenda> itemExistente = vendaAtual.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().setQuantidade(itemExistente.get().getQuantidade() + quantidade);
        } else {
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
        Produto produto = produtoRepository.findById(id).orElse(null);
        model.addAttribute("produto", produto);
        return new ModelAndView("produto/form", model);
    }

    /**
     * MÉTODO SEPARADO: SALVAR (INSERT)
     */
    @PostMapping("/save")
    public String save(@Valid Produto produto, BindingResult result, RedirectAttributes attr) {
        if (result.hasErrors()) {
            // Retorna para o formulário se houver erros de validação
            return "produto/form";
        }
        produtoRepository.save(produto);
        attr.addFlashAttribute("success", "Produto cadastrado com sucesso!");
        return "redirect:/produto/list";
    }

    /**
     * MÉTODO SEPARADO: ATUALIZAR (UPDATE)
     */
    @PostMapping("/update")
    public String update(@Valid Produto produto, BindingResult result, RedirectAttributes attr) {
        if (result.hasErrors()) {
            // Retorna para o formulário se houver erros de validação
            return "produto/form";
        }
        produtoRepository.save(produto);
        attr.addFlashAttribute("success", "Produto atualizado com sucesso!");
        return "redirect:/produto/list";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id, RedirectAttributes attr) {
        try {
            produtoRepository.deleteById(id);
            attr.addFlashAttribute("success", "Produto removido!");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "Não é possível remover um produto vinculado a vendas.");
        }
        return "redirect:/produto/list";
    }
}