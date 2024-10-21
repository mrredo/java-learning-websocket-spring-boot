package org.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class mainpage {
    @GetMapping("/")
    public String testMain(Model model) {
        return "hello"; // Returns hello.html
    }

}
