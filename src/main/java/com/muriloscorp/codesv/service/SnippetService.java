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
        CodeSnippet snippet = SnippetRequest.toEntity(request);
        snippetRepository.save(snippet);
    }

    public List<CodeSnippet> findAll(){
        return snippetRepository.findAll();
    }

    public CodeSnippet findById(UUID id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new SnippetNotFoundException("Snippet não encontrado"));
    }

    public void update(UUID id, SnippetRequest request) {
        CodeSnippet oldSnippet = this.findById(id);
        CodeSnippet newSnippet = SnippetRequest.updateEntity(oldSnippet, request);
        snippetRepository.save(newSnippet);
    }

    public void delete(UUID id) {
        if (!snippetRepository.existsById(id)) {
            throw new SnippetNotFoundException("Snippet não encontrado");
        }
        snippetRepository.deleteById(id);
    }
}
