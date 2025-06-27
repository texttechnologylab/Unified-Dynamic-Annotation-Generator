package uni.textimager.sandbox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Welcome");
        return "index";
    }

    @GetMapping("/chart")
    public String chart(Model model) {
        model.addAttribute("title", "Basic pie chart");
        return "chart";
    }
}
