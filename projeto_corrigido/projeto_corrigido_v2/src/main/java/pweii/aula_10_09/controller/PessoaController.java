package pweii.aula_10_09.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pweii.aula_10_09.model.entity.*;
import pweii.aula_10_09.model.repository.PessoaRepository;
import pweii.aula_10_09.model.repository.RoleRepository;
import pweii.aula_10_09.model.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;

@Transactional
@Controller
@RequestMapping("/pessoa")
public class PessoaController {

    @Autowired
    PessoaRepository pessoaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addAttributes(ModelMap model) {
        if (!model.containsAttribute("pessoaFisica")) {
            model.addAttribute("pessoaFisica", new PessoaFisica());
        }
        if (!model.containsAttribute("pessoaJuridica")) {
            model.addAttribute("pessoaJuridica", new PessoaJuridica());
        }
    }

    @GetMapping("/formPF")
    public String formPF(ModelMap model) {
        model.addAttribute("tipoPessoa", "PF");
        return "pessoa/form";
    }

    @GetMapping("/formPJ")
    public String formPJ(ModelMap model) {
        model.addAttribute("tipoPessoa", "PJ");
        return "pessoa/form";
    }

    @PostMapping("/savePF")
    public ModelAndView savePF(@Valid PessoaFisica pessoaFisica,
                               BindingResult result,
                               @RequestParam("login") String login,
                               @RequestParam("password") String password,
                               RedirectAttributes attr) {

        if (pessoaFisica.getCpf() != null) {
            pessoaFisica.setCpf(pessoaFisica.getCpf().replaceAll("[^0-9]", ""));
        }

        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("pessoa/form");
            mv.addObject("tipoPessoa", "PF");
            return mv;
        }

        if (usuarioRepository.findByLogin(login) != null) {
            ModelAndView mv = new ModelAndView("pessoa/form");
            mv.addObject("tipoPessoa", "PF");
            mv.addObject("error", "Este login já está em uso!");
            return mv;
        }

        // 1. Primeiro criamos e salvamos o Usuário (ele precisa de um ID agora)
        Usuario novoUsuario = criarUsuarioParaPessoa(pessoaFisica, login, password);

        // 2. Vinculamos o Usuário à Pessoa
        // Isso garante que a coluna 'usuario_id' na tabela 'pessoas' receba o valor correto
        pessoaFisica.setUsuario(novoUsuario);

        // 3. Salvamos a Pessoa por último
        pessoaRepository.save(pessoaFisica);

        attr.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login para continuar.");
        return new ModelAndView("redirect:/login");
    }

    @PostMapping("/savePJ")
    public ModelAndView savePJ(@Valid PessoaJuridica pessoaJuridica,
                               BindingResult result,
                               @RequestParam("login") String login,
                               @RequestParam("password") String password,
                               RedirectAttributes attr) {

        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("pessoa/form");
            mv.addObject("tipoPessoa", "PJ");
            return mv;
        }

        if (usuarioRepository.findByLogin(login) != null) {
            ModelAndView mv = new ModelAndView("pessoa/form");
            mv.addObject("tipoPessoa", "PJ");
            mv.addObject("error", "Este login já está em uso!");
            return mv;
        }

        // 1. Primeiro criamos e salvamos o Usuário
        Usuario novoUsuario = criarUsuarioParaPessoa(pessoaJuridica, login, password);

        // 2. Vinculamos o Usuário à Pessoa Jurídica
        pessoaJuridica.setUsuario(novoUsuario);

        // 3. Salvamos a Pessoa (a coluna usuario_id será preenchida)
        pessoaRepository.save(pessoaJuridica);

        attr.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login para continuar.");
        return new ModelAndView("redirect:/login");
    }

    private Usuario criarUsuarioParaPessoa(Pessoa pessoa, String login, String password) {
        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setPassword(passwordEncoder.encode(password));

        // Vínculo no sentido Usuário -> Pessoa
        usuario.setPessoa(pessoa);

        // Busca ou cria a Role USER
        Role roleUser = roleRepository.findByNome("ROLE_USER");
        if (roleUser == null) {
            roleUser = new Role();
            roleUser.setNome("ROLE_USER");
            roleUser = roleRepository.save(roleUser);
        }

        usuario.setRoles(Collections.singletonList(roleUser));

        // Salva o usuário no banco e retorna o objeto com ID gerado
        return usuarioRepository.save(usuario);
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model) {
        Pessoa pessoa = pessoaRepository.findById(id).orElse(null);
        if (pessoa == null) return new ModelAndView("redirect:/pessoa/list");

        if (pessoa instanceof PessoaFisica) {
            model.addAttribute("pessoaFisica", (PessoaFisica) pessoa);
            model.addAttribute("tipoPessoa", "PF");
        } else {
            model.addAttribute("pessoaJuridica", (PessoaJuridica) pessoa);
            model.addAttribute("tipoPessoa", "PJ");
        }
        return new ModelAndView("pessoa/form", model);
    }

    @GetMapping({"/list", "/filter"})
    public ModelAndView listarOuFiltrar(@RequestParam(value = "nome", required = false) String nome, ModelMap model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) return new ModelAndView("redirect:/produto/list");

        List<Pessoa> pessoas;
        if (nome != null && !nome.trim().isEmpty()) {
            pessoas = pessoaRepository.findByNomeOrRazaoSocialContaining("%" + nome.trim() + "%");
            model.addAttribute("nome", nome);
        } else {
            pessoas = pessoaRepository.findAll();
            model.addAttribute("nome", "");
        }

        model.addAttribute("pessoas", pessoas);
        return new ModelAndView("pessoa/list", model);
    }

    @GetMapping("/remove/{id}")
    public ModelAndView remove(@PathVariable("id") Long id, RedirectAttributes attr) {
        pessoaRepository.deleteById(id);
        attr.addFlashAttribute("success", "Cliente excluído com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    @GetMapping("/vendas/{id}")
    public ModelAndView vendasCliente(@PathVariable("id") Long id, ModelMap model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        Usuario usuario = usuarioRepository.findByLogin(login);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Pessoa cliente = pessoaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!isAdmin && (usuario == null || usuario.getPessoa() == null || !usuario.getPessoa().getId().equals(id))) {
            return new ModelAndView("redirect:/produto/list");
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("vendas", cliente.getVendas());
        return new ModelAndView("pessoa/vendas", model);
    }
}
