package com.codecollab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "redirect:/doc.html";
    }

    @GetMapping("/api")
    public String apiIndex() {
        return "redirect:/doc.html";
    }
}
