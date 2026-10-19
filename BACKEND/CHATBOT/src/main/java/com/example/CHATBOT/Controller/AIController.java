
package com.example.CHATBOT.Controller;

import com.example.CHATBOT.Service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins="*")
public class AIController {

    private final AIService serv;

    public AIController(AIService serv) {
        this.serv = serv;
    }

    @PostMapping("/chat")
    public String call(@RequestBody ChatRequest request) {
        return serv.chat(request.message());
    }

    public record ChatRequest(String message) {}
}
