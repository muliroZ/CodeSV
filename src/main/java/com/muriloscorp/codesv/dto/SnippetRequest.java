package com.muriloscorp.codesv.dto;

import com.muriloscorp.codesv.model.CodeSnippet;
import jakarta.validation.constraints.NotBlank;

public record SnippetRequest(
        @NotBlank(message = "O título é obrigatório") String title,
        @NotBlank(message = "O conteúdo não pode estar vazio") String content,
        @NotBlank(message = "Selecione uma linguagem") String language
) {
    public static SnippetRequest empty() {
        return new SnippetRequest("", "", "");
    }

    public static SnippetRequest toRequest(CodeSnippet entity) {
        return new SnippetRequest(
                entity.getTitle(),
                entity.getContent(),
                entity.getLanguage()
        );
    }

    public static CodeSnippet toEntity(SnippetRequest request) {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setTitle(request.title());
        snippet.setLanguage(request.language());
        snippet.setContent(request.content());
        return snippet;
    }

    public static CodeSnippet updateEntity(CodeSnippet entity, SnippetRequest request) {
        entity.setTitle(request.title());
        entity.setLanguage(request.language());
        entity.setContent(request.content());
        return entity;
    }
}
