package com.kk.brainbuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving the frontend
 */
@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}