package com.muriloscorp.codesv.controller;

import com.muriloscorp.codesv.dto.SnippetRequest;
import com.muriloscorp.codesv.exception.UserNotFoundException;
import com.muriloscorp.codesv.model.CodeSnippet;
import com.muriloscorp.codesv.model.User;
import com.muriloscorp.codesv.repository.UserRepository;
import com.muriloscorp.codesv.service.RateLimitingService;
import com.muriloscorp.codesv.service.SnippetService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    private final UserRepository userRepository;
    private final RateLimitingService rateLimitingService;

    public SnippetController(SnippetService snippetService, UserRepository userRepository, RateLimitingService rateLimitingService) {
        this.snippetService = snippetService;
        this.userRepository = userRepository;
        this.rateLimitingService = rateLimitingService;
    }

    public boolean isOwner(OAuth2User principal, CodeSnippet snippet) {
        if (principal == null || snippet == null) return false;

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();
        return snippet.getAuthor().getGithubId().equals(githubId);
    }

    @GetMapping
    public String listAll(
            Model model, @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(name = "filter", required = false, defaultValue = "my") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<CodeSnippet> snippetPage;

        if (principal == null) {
            snippetPage = snippetService.findAll(pageable);
            model.addAttribute("snippets", snippetPage);
            model.addAttribute("pageTitle", "Explorar Comunidade");
            model.addAttribute("activeFilter", "all");
            return "snippet-list";
        }

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserNotFoundException("Erro de sessão."));

        if ("all".equals(filter)) {
            snippetPage = snippetService.findAll(pageable);
            model.addAttribute("snippets", snippetPage);
            model.addAttribute("pageTitle", "Explorar Comunidade");
            model.addAttribute("activeFilter", "all");
            model.addAttribute("isPersonalView", false);
        } else {
            snippetPage = snippetService.findByAuthorId(user.getId(), pageable);
            model.addAttribute("snippets", snippetPage);
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
                              Model model,
                              @AuthenticationPrincipal OAuth2User principal
                              ) {
        if (result.hasErrors()) {
            return "snippet-form";
        }

        Long githubId = ((Number) principal.getAttributes().get("id")).longValue();

        String userId = String.valueOf(githubId);
        Bucket bucket = rateLimitingService.resolveBucket(userId);

        if (!bucket.tryConsume(1)) {
            model.addAttribute("error", "Você atingiu o limite de criação (5 snippets/min). Aguarde um pouco.");
            return "snippet-form";
        }

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
    public ResponseEntity<ByteArrayResource> downloadSnippet(
            @PathVariable UUID id,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        CodeSnippet snippet = snippetService.findById(id);
        if (!snippet.isPublic() && !isOwner(principal, snippet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para baixar esse código");
        }

        String extension = switch (snippet.getLanguage().toLowerCase()) {
            case "java" -> ".java";
            case "python" -> ".py";
            case "javascript" -> ".js";
            case "cpp" -> ".cpp";
            case "sql" -> ".sql";
            default -> ".txt";
        };

        String filename = snippet.getTitle().replaceAll("[^a-zA-Z0-9._-]", "_") + extension;

        byte[] data = snippet.getContent().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
