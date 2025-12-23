package com.muriloscorp.codesv.service;

import com.muriloscorp.codesv.dto.SnippetRequest;
import com.muriloscorp.codesv.exception.SnippetNotFoundException;
import com.muriloscorp.codesv.model.CodeSnippet;
import com.muriloscorp.codesv.repository.SnippetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SnippetService {

    private final SnippetRepository snippetRepository;

    public SnippetService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    public void save(SnippetRequest request){
        CodeSnippet snippet = new CodeSnippet();
        snippet.setTitle(request.title());
        snippet.setContent(request.content());
        snippet.setLanguage(request.language());
        snippetRepository.save(snippet);
    }

    public List<CodeSnippet> findAll(){
        return snippetRepository.findAll();
    }

    public CodeSnippet findById(UUID id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new SnippetNotFoundException("Snippet não encontrado"));
    }
}
