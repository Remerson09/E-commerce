package pweii.aula_10_09.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pweii.aula_10_09.model.entity.*;
import pweii.aula_10_09.model.repository.PessoaRepository;
import pweii.aula_10_09.model.repository.UsuarioRepository;
import pweii.aula_10_09.model.repository.VendaRepository;

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

    @Autowired
    UsuarioRepository usuarioRepository;

    @GetMapping("/list")
    public ModelAndView listar(ModelMap model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        Usuario usuario = usuarioRepository.findByLogin(login);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("vendas", vendaRepository.findAll());
        } else if (usuario != null && usuario.getPessoa() != null) {
            model.addAttribute("vendas", vendaRepository.findByCliente(usuario.getPessoa()));
        }

        return new ModelAndView("vendas/list", model);
    }

    @GetMapping("/carrinho")
    public ModelAndView verCarrinho(@ModelAttribute("vendaEmAndamento") Venda venda, ModelMap model) {
        BigDecimal totalCalculado = venda.getItens().stream()
                .map(item -> item.getProduto().getValor().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("vendaAtual", venda);
        model.addAttribute("totalVenda", totalCalculado);

        return new ModelAndView("vendas/carrinho", model);
    }

    @PostMapping("/finalizar")
    public String finalizar(@RequestParam("descricao") String descricao,
                            @ModelAttribute("vendaEmAndamento") Venda venda,
                            SessionStatus status,
                            RedirectAttributes attr) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        Usuario usuario = usuarioRepository.findByLogin(login);

        if (usuario == null || usuario.getPessoa() == null) {
            attr.addFlashAttribute("error", "Você precisa estar logado e ter um cadastro de cliente para finalizar a venda!");
            return "redirect:/vendas/carrinho";
        }

        if (venda.getItens().isEmpty()) {
            attr.addFlashAttribute("error", "Seu carrinho está vazio!");
            return "redirect:/vendas/carrinho";
        }

        venda.setId(null);
        venda.setCliente(usuario.getPessoa());
        venda.setDataVenda(LocalDateTime.now());
        venda.setDescricao(descricao);

        for (ItemVenda item : venda.getItens()) {
            item.setVenda(venda);
            item.setPrecoUnitario(item.getProduto().getValor());
            item.setId(null);
        }

        vendaRepository.save(venda);
        status.setComplete();

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
        Optional<Venda> busca = vendaRepository.findById(id);
        if (busca.isPresent()) {
            model.addAttribute("venda", busca.get());
        } else {
            model.addAttribute("venda", new Venda());
        }
        return new ModelAndView("vendas/detalhe", model);
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Long id) {
        vendaRepository.deleteById(id);
        return "redirect:/vendas/list";
    }
}
