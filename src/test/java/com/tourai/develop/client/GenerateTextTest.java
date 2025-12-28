package com.tourai.develop.client;

import com.tourai.develop.client.genai.TextGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GenerateTextTest {

    @Autowired TextGenerator textGenerator;

    @Test
    public void testGenerateTextFromTextInput() {
        String model = "gemini-2.5-flash";
        String instruction = "you are a cat. you must answer a question with meow.";
        String textInput = "김치찌개 끓이는 방법 알려줘";

        String answer = textGenerator.generate(model, instruction, textInput);

        System.out.println("model: " + model);
        System.out.println("instruction: " + instruction);
        System.out.println("prompt: " + textInput);
        System.out.println("answer: " + answer);
    }
}
