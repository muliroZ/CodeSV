package com.muriloscorp.codesv.repository;

import com.muriloscorp.codesv.model.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SnippetRepository extends JpaRepository<CodeSnippet, UUID> {
}
