package com.tourai.develop.client.genai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiTextGenerator implements TextGenerator {

    private final Client client;

    public GeminiTextGenerator(@Value("${gemini.api-key}") String apiKey) {
        this.client = Client.builder().apiKey(apiKey).build();
    }

    @Override
    public String generate(String model, String instruction, String textInput) {
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .systemInstruction(Content.fromParts(Part.fromText(instruction)))
                        .build();

        GenerateContentResponse response = client.models.generateContent(
                model,
                textInput,
                config
        );

        return response.text();
    }

}
