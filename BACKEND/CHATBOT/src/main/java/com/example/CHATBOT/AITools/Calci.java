package com.example.CHATBOT.AITools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class Calci {

    @Tool(description = "perform arithmetic operations like add, subtract, multiply, divide, mod, power")
    public String calcuate(
            @ToolParam(description = "add, subtract, multiply, divide, mod, power") String operation,
            @ToolParam(description = "first operand") double a,
            @ToolParam(description = "second operand") double b) {

        if (operation == null) {
            return "Can't execute this request: operation was not provided. Please try again.";
        }

        if (operation.equalsIgnoreCase("add"))
            return String.valueOf(a + b);
        if (operation.equalsIgnoreCase("subtract"))
            return String.valueOf(a - b);
        if (operation.equalsIgnoreCase("divide")) {
            if (b != 0)
                return String.valueOf(a / b);
            else
                return "Can't execute this request: cannot divide by zero.";
        }
        if (operation.equalsIgnoreCase("mod")) {
            if (b != 0)
                return String.valueOf(a % b);
            else
                return "Can't execute this request: cannot calculate modulo by zero.";
        }
        if (operation.equalsIgnoreCase("power"))
            return String.valueOf(Math.pow(a, b));
        if (operation.equalsIgnoreCase("multiply"))
            return String.valueOf(a * b);

        return "can't execute this request ,please try again";
    }
}