package Shivu.ArbitraAI.config;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AgentConfig {

    @Value("${app.openai.api-key}")
    private String openAiApiKey;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(openAiApiKey)
                .modelName("openai/gpt-oss-20b")
                .temperature(0.1)
                .timeout(Duration.ofSeconds(90))
                .build();
    }
}