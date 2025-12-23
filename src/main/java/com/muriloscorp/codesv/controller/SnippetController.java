package com.muriloscorp.codesv.controller;

import com.muriloscorp.codesv.dto.SnippetRequest;
import com.muriloscorp.codesv.exception.UserNotFoundException;
import com.muriloscorp.codesv.model.CodeSnippet;
import com.muriloscorp.codesv.model.User;
import com.muriloscorp.codesv.repository.UserRepository;
import com.muriloscorp.codesv.service.SnippetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    private final UserRepository userRepository;

    public SnippetController(SnippetService snippetService, UserRepository userRepository) {
        this.snippetService = snippetService;
        this.userRepository = userRepository;
    }

    public boolean isOwner(OAuth2User principal, CodeSnippet snippet) {
        if (principal == null || snippet == null) return false;

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();
        return snippet.getAuthor().getGithubId().equals(githubId);
    }

    @GetMapping
    public String listAll(
            Model model, @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(name = "filter", required = false, defaultValue = "my") String filter
    ) {
        if (principal == null) {
            model.addAttribute("snippets", snippetService.findAll());
            model.addAttribute("pageTitle", "Explorar Comunidade");
            model.addAttribute("activerFilter", "all");
            return "snippet-list";
        }

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserNotFoundException("Erro de sessão."));

        if ("all".equals(filter)) {
            model.addAttribute("snippets", snippetService.findAll());
            model.addAttribute("pageTitle", "Explorar Comunidade");
            model.addAttribute("activeFilter", "all");
            model.addAttribute("isPersonalView", false);
        } else {
            model.addAttribute("snippets", snippetService.findByAuthorId(user.getId()));
            model.addAttribute("pageTitle", "Meus Snippets");
            model.addAttribute("activeFilter", "my");
            model.addAttribute("isPersonalView", true);
        }

        return "snippet-list";
    }

    @GetMapping("/{id}")
    public String viewSnippet(
            @PathVariable UUID id, Model model,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        CodeSnippet snippet = snippetService.findById(id);
        boolean isOwner = isOwner(principal, snippet);

        model.addAttribute("snippet", snippet);
        model.addAttribute("isOwner", isOwner);
        return "snippet-view";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("snippetForm", SnippetRequest.empty());
        return "snippet-form";
    }

    @PostMapping("/save")
    public String saveSnippet(@Valid @ModelAttribute("snippetForm") SnippetRequest request,
                              BindingResult result, RedirectAttributes redirect,
                              @AuthenticationPrincipal OAuth2User principal
                              ) {
        if (result.hasErrors()) {
            return "snippet-form";
        }

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();
        User author = userRepository.findByGithubId(githubId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        snippetService.save(request, author);
        redirect.addFlashAttribute("success", "Snippet criado com sucesso!");

        return "redirect:/snippets";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        CodeSnippet snippet = snippetService.findById(id);

        SnippetRequest form = SnippetRequest.toRequest(snippet);
        model.addAttribute("snippetForm", form);
        model.addAttribute("snippetId", id);
        return "snippet-form";
    }

    @PostMapping("/{id}/update")
    public String updateSnippet(
            @PathVariable UUID id,
            @Valid @ModelAttribute("snippetForm") SnippetRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        if (result.hasErrors()) {
            model.addAttribute("snippetId", id);
            return "snippet-form";
        }

        CodeSnippet snippet = snippetService.findById(id);
        if (!isOwner(principal, snippet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso Negado.");
        }

        snippetService.update(id, request);
        redirect.addFlashAttribute("success", "Snippet atualizado com sucesso!");

        return "redirect:/snippets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteSnippet(
            @PathVariable UUID id, RedirectAttributes redirect,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        CodeSnippet snippet = snippetService.findById(id);
        if (!isOwner(principal, snippet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir esse snippet.");
        }

        snippetService.delete(id);
        redirect.addFlashAttribute("success", "Snippet excluído com sucesso!");
        return "redirect:/snippets";
    }

    @GetMapping("/{id}/download")
    public String downloadSnippet(@PathVariable UUID id) {}
}
