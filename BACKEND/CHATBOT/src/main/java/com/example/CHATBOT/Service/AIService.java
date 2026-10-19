
package com.example.CHATBOT.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    private final ChatClient client;

    private final List<Message> history = new ArrayList<>();

    public AIService(ChatClient.Builder builder) {
        this.client = builder.build();
    }

    public String chat(String msg) {

        history.add(new UserMessage(msg));

        String output = client.prompt()
                .messages(history)
                .call()
                .content();

        history.add(new AssistantMessage(output));

        return output;
    }
}
