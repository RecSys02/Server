package com.tourai.develop.client.genai;

public interface TextGenerator {
    GenAiResponse generate(String model, String instruction, String textInput);
}
