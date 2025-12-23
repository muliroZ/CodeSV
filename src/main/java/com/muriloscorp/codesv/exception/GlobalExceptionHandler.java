package com.muriloscorp.codesv.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SnippetNotFoundException.class)
    public ResponseEntity<?> snippetNotFoundHandler(SnippetNotFoundException ex){
        return ResponseEntity
                .status(404)
                .body(ex.getMessage());
    }
}
