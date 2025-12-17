package pweii.aula_10_09.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pweii.aula_10_09.model.entity.Pessoa;
import pweii.aula_10_09.model.entity.PessoaFisica;
import pweii.aula_10_09.model.entity.PessoaJuridica;
import pweii.aula_10_09.model.repository.PessoaRepository;

import java.util.List;

@Transactional
@Controller
@RequestMapping("/pessoa")
public class PessoaController {

    @Autowired
    PessoaRepository pessoaRepository;

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

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id, ModelMap model) {
        // Usamos findById().orElse() para evitar falha se o ID não existir
        Pessoa pessoa = pessoaRepository.findById(id).orElse(null);

        if (pessoa == null) {
            // Opcional: lidar com pessoa não encontrada
            return new ModelAndView("redirect:/pessoa/list");
        }

        if (pessoa instanceof PessoaFisica) {
            model.addAttribute("pessoaFisica", (PessoaFisica) pessoa);
            model.addAttribute("tipoPessoa", "PF");
        } else {
            model.addAttribute("pessoaJuridica", (PessoaJuridica) pessoa);
            model.addAttribute("tipoPessoa", "PJ");
        }

        return new ModelAndView("pessoa/form", model);
    }

    // Adicione o @Valid antes da entidade e o BindingResult depois dela.
    @PostMapping("/savePF")
    public ModelAndView savePF(@Valid PessoaFisica pessoaFisica, BindingResult result, RedirectAttributes attr) {

        // 1. Limpeza do CPF (pré-processamento)
        if (pessoaFisica.getCpf() != null) {
            // Remove pontos e hífens do CPF
            String cpfLimpo = pessoaFisica.getCpf().replaceAll("[^0-9]", "");
            pessoaFisica.setCpf(cpfLimpo);
        }

        // 2. Verificação de Erros
        // O BindingResult 'result' já capturou os erros de validação (@CPF, @NotBlank, etc.)
        // que ocorreram APÓS a limpeza do CPF.
        // No seu PessoaController, dentro do método savePF:
        if (result.hasErrors()) {
            // Certifique-se de que "cadastro" é o nome correto do arquivo HTML (sem a extensão .html)
            return new ModelAndView("cadastro");
        }

        // 3. Salvamento (Executado apenas se não houver erros)
        pessoaRepository.save(pessoaFisica);

        // 4. Sucesso e Redirecionamento
        attr.addFlashAttribute("success", "Pessoa Física salva com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    @PostMapping("/savePJ")
    public ModelAndView savePJ(PessoaJuridica pessoaJuridica, RedirectAttributes attr) {
        pessoaRepository.save(pessoaJuridica);
        attr.addFlashAttribute("success", "Pessoa Jurídica salva com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    @PostMapping("/updatePF")
    // 💡 CORREÇÃO 1: Adicionar @Valid e BindingResult
    public ModelAndView updatePF(@Valid PessoaFisica pessoaFisica, BindingResult result, RedirectAttributes attr) {

        // 💡 CORREÇÃO 2: Limpeza do CPF (necessário para validação)
        if (pessoaFisica.getCpf() != null) {
            String cpfLimpo = pessoaFisica.getCpf().replaceAll("[^0-9]", "");
            pessoaFisica.setCpf(cpfLimpo);
        }

        // 💡 CORREÇÃO 3: Tratamento de Erros de Validação
        if (result.hasErrors()) {
            // Se houver erros, adiciona o objeto com erro no model (via FlashAttribute)
            attr.addFlashAttribute("pessoaFisica", pessoaFisica);

            // Adiciona a mensagem de erro específica para o usuário
            String mensagemErro = "Erro de validação! Corrija os campos e tente novamente.";
            if (result.hasFieldErrors("cpf")) {
                mensagemErro = "CPF inválido.";
            }
            attr.addFlashAttribute("error", mensagemErro);

            // Retorna para o formulário de edição (com o ID para manter o contexto)
            return new ModelAndView("redirect:/pessoa/edit/" + pessoaFisica.getId());
        }

        // Se não houver erros, salva (faz o update)
        pessoaRepository.save(pessoaFisica);
        attr.addFlashAttribute("success", "Pessoa Física atualizada com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    @PostMapping("/updatePJ")
    public ModelAndView updatePJ(PessoaJuridica pessoaJuridica, RedirectAttributes attr) {
        pessoaRepository.save(pessoaJuridica);
        attr.addFlashAttribute("success", "Pessoa Jurídica atualizada com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    // ------------------------------------------------
    // 🔍 Listagem e Filtro (UNIFICADOS)
    // ------------------------------------------------

    // Mapeia tanto para '/pessoa/list' (sem filtro) quanto para '/pessoa/filter'
    @GetMapping({"/list", "/filter"})
    public ModelAndView listarOuFiltrar(@RequestParam(value = "nome", required = false) String nome,
                                        ModelMap model) {

        List<Pessoa> pessoas;

        if (nome != null && !nome.trim().isEmpty()) {
            // CORREÇÃO CRÍTICA: Adiciona o wildcard '%' para a busca LIKE no repositório.
            String termoBusca = "%" + nome.trim() + "%";
            pessoas = pessoaRepository.findByNomeOrRazaoSocialContaining(termoBusca);

            // Adiciona o termo original de volta para preencher o input do filtro no HTML
            model.addAttribute("nome", nome);
        } else {
            // Se não houver filtro, lista todos (método list padrão)
            pessoas = pessoaRepository.findAll();
            model.addAttribute("nome", ""); // Garante que o campo de filtro esteja vazio
        }

        model.addAttribute("pessoas", pessoas);
        return new ModelAndView("pessoa/list", model);
    }

    // ------------------------------------------------
    // 🗑️ Exclusão e Vendas
    // ------------------------------------------------

    @GetMapping("/remove/{id}")
    public ModelAndView remove(@PathVariable("id") Long id, RedirectAttributes attr) {
        pessoaRepository.deleteById(id);
        attr.addFlashAttribute("info", "Cliente excluído com sucesso!");
        return new ModelAndView("redirect:/pessoa/list");
    }

    @GetMapping("/vendas/{id}")
    public ModelAndView vendasCliente(@PathVariable("id") Long id, ModelMap model) {
        Pessoa cliente = pessoaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        model.addAttribute("cliente", cliente);
        // Assumindo que o campo 'vendas' na entidade Pessoa é carregado corretamente
        model.addAttribute("vendas", cliente.getVendas());
        return new ModelAndView("pessoa/vendas", model);
    }
}