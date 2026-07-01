package cn.org.alan.exam.config;

import cn.org.alan.exam.utils.agent.AIChat;
import cn.org.alan.exam.utils.agent.impl.MockAIChat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfiguration {

    @Bean
    @ConditionalOnMissingBean(AIChat.class)
    @ConditionalOnProperty(prefix = "ai", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
    public AIChat mockAIChat() {
        return new MockAIChat();
    }
}
