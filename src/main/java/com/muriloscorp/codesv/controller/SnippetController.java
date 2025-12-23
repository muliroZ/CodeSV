package com.muriloscorp.codesv.controller;

import com.muriloscorp.codesv.dto.SnippetRequest;
import com.muriloscorp.codesv.model.CodeSnippet;
import com.muriloscorp.codesv.service.SnippetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;

    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @GetMapping
    public String listAll(Model model) {
        model.addAttribute("snippets", snippetService.findAll());
        return "snippet-list";
    }

    @GetMapping("/{id}")
    public String viewSnippet(@PathVariable UUID id, Model model) {
        CodeSnippet snippet = snippetService.findById(id);
        model.addAttribute("snippet", snippet);
        return "snippet-view";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("snippetForm", SnippetRequest.empty());
        return "snippet-form";
    }

    @PostMapping("/save")
    public String saveSnippet(@Valid @ModelAttribute("snippetForm") SnippetRequest request,
                              BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "snippet-form";
        }
        snippetService.save(request);
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
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("snippetId", id);
            return "snippet-form";
        }

        snippetService.update(id, request);
        redirect.addFlashAttribute("success", "Snippet atualizado com sucesso!");

        return "redirect:/snippets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteSnippet(@PathVariable UUID id, RedirectAttributes redirect) {
        snippetService.delete(id);
        redirect.addFlashAttribute("success", "Snippet excluído com sucesso!");
        return "redirect:/snippets";
    }
}
