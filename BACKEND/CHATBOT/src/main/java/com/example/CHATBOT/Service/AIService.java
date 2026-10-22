
package com.example.CHATBOT.Service;

import com.example.CHATBOT.AITools.Calci;
import com.example.CHATBOT.AITools.CurrencyTools;
import com.example.CHATBOT.AITools.Wthr;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    private final ChatClient client;

    private final List<Message> history = new ArrayList<>();
    @Autowired
    Calci cal;
    @Autowired
    CurrencyTools cur;
    @Autowired
    Wthr weather;
    public AIService(ChatClient.Builder builder) {
        this.client = builder.build();
    }

    public String chat(String msg) {

        history.add(new UserMessage(msg));

        String output = client.prompt()
                .messages(history)
                .tools(cur,cal,weather)
                .call()
                .content();

        history.add(new AssistantMessage(output));

        return output;
    }
}
