package com.muriloscorp.codesv.dto;

import jakarta.validation.constraints.NotBlank;

public record SnippetRequest(
        @NotBlank(message = "O título é obrigatório") String title,
        @NotBlank(message = "O conteúdo não pode estar vazio") String content,
        @NotBlank(message = "Selecione uma linguagem") String language
) {
    public static SnippetRequest empty() {
        return new SnippetRequest("", "", "");
    }
}
