package com.muriloscorp.codesv.controller;

import com.muriloscorp.codesv.dto.SnippetRequest;
import com.muriloscorp.codesv.model.CodeSnippet;
import com.muriloscorp.codesv.service.SnippetService;
import jakarta.validation.Valid;
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
        redirect.addFlashAttribute("success", "Snippet created successfully!");

        return "redirect:/snippets";
    }
}
