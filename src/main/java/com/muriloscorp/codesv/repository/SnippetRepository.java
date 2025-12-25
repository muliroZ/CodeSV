package com.muriloscorp.codesv.repository;

import com.muriloscorp.codesv.model.CodeSnippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SnippetRepository extends JpaRepository<CodeSnippet, UUID> {
    Page<CodeSnippet> findByAuthorId(UUID authorId, Pageable pageable);
    Page<CodeSnippet> findByIsPublic(boolean isPublic, Pageable pageable);
}
