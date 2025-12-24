package com.muriloscorp.codesv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(){
        return "snippet-list";
    }

    @GetMapping("/about")
    public String about(){
        return "about";
    }
}
