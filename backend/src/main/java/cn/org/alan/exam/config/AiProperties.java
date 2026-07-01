package cn.org.alan.exam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider = "mock";

    private String baseUrl;

    private String apiKey;

    private String model;

    private Boolean enabled = false;

    private Boolean mockEnabled = true;

    private String questionGeneratorServiceUrl;

    public boolean isAiEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isMockMode() {
        return Boolean.TRUE.equals(mockEnabled);
    }
}
