package pweii.aula_10_09.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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

    // Mantém os objetos no modelo para as telas de edição
    @ModelAttribute
    public void addAttributes(ModelMap model) {
        if (!model.containsAttribute("pessoaFisica")) {
            model.addAttribute("pessoaFisica", new PessoaFisica());
        }
        if (!model.containsAttribute("pessoaJuridica")) {
            model.addAttribute("pessoaJuridica", new PessoaJuridica());
        }
    }

    @GetMapping("/form")
    public String form(ModelMap model) {
        if (!model.containsAttribute("tipoPessoa")) {
            model.addAttribute("tipoPessoa", "PF");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Usuario usuario = usuarioRepository.findByLogin(auth.getName());
            if (usuario != null && usuario.getPessoa() == null) {
                model.addAttribute("info", "Você está logado como " + usuario.getLogin() + ". Complete seu cadastro abaixo.");
            }
        }
        return "pessoa/form";
    }

    // Método unificado para salvar vindo do formulário genérico
    @PostMapping("/save")
    public ModelAndView save(@RequestParam("identificador") String identificador,
                             @RequestParam("nomeRazao") String nomeRazao,
                             @RequestParam("email") String email,
                             @RequestParam("telefone") String telefone,
                             @RequestParam("login") String login,
                             @RequestParam("password") String password,
                             RedirectAttributes attr) {

        String cleanId = identificador.replaceAll("[^0-9]", "");
        Pessoa pessoa;

        if (cleanId.length() == 11) {
            PessoaFisica pf = new PessoaFisica();
            pf.setNome(nomeRazao);

            pf.setEmail(email);
            pf.setTelefone(telefone);
            pf.setCpf(cleanId);
            pessoa = pf;
        } else if (cleanId.length() == 14) {
            PessoaJuridica pj = new PessoaJuridica();
            pj.setRazaoSocial(nomeRazao);
            pj.setCnpj(cleanId);
            pj.setEmail(email);
            pj.setTelefone(telefone);
            pessoa = pj;
        } else {
            attr.addFlashAttribute("error", "CPF ou CNPJ inválido!");
            return new ModelAndView("redirect:/pessoa/form");
        }

        // Verifica se o login já existe antes de criar
        if (usuarioRepository.findByLogin(login) != null) {
            attr.addFlashAttribute("error", "Este login já está em uso!");
            return new ModelAndView("redirect:/pessoa/form");
        }

        pessoaRepository.save(pessoa);
        criarUsuarioParaPessoa(pessoa, login, password);

        attr.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login.");
        return new ModelAndView("redirect:/login");
    }

    private void criarUsuarioParaPessoa(Pessoa pessoa, String login, String password) {
        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setPessoa(pessoa);

        Role roleUser = roleRepository.findByNome("ROLE_USER");
        if (roleUser == null) {
            roleUser = new Role();
            roleUser.setNome("ROLE_USER");
            roleRepository.save(roleUser);
        }
        usuario.setRoles(Collections.singletonList(roleUser));
        usuarioRepository.save(usuario);
    }

    // --- MÉTODOS DE ADMINISTRAÇÃO E VISUALIZAÇÃO ---

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

        List<Pessoa> pessoas = (nome != null && !nome.trim().isEmpty())
                ? pessoaRepository.findByNomeOrRazaoSocialContaining("%" + nome.trim() + "%")
                : pessoaRepository.findAll();

        model.addAttribute("pessoas", pessoas);
        model.addAttribute("nome", nome != null ? nome : "");
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
        Usuario usuario = usuarioRepository.findByLogin(auth.getName());
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