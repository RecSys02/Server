package com.tourai.develop.client.genai;

public interface TextGenerator {
    String generate(String model, String instruction, String textInput);
}
