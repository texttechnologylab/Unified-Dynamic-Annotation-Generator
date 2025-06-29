package uni.textimager.sandbox.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AppController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Welcome");
        return "index";
    }

    @GetMapping("/chart")
    public String chart(Model model) throws JsonProcessingException {
        List<TestObject> testObjectList = new ArrayList<>();
        testObjectList.add(new TestObject("test", "123"));
        testObjectList.add(new TestObject("test", "123"));
        testObjectList.add(new TestObject("test", "123"));
        model.addAttribute("title", new ObjectMapper().writeValueAsString(testObjectList));
        return "chart";
    }
}
