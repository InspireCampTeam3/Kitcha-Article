package com.kitcha.article.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    // application.yml에 정의된 openai.api-key 값을 주입받음
    @Value("${openai.api-key}")
    private String apiKey;

    /**
     * ChatClient 빈을 생성하는 메소드
     * ChatClient는 OpenAI Chat API와 상호작용하여 대화형 응답을 생성하는 클라이언트입니다.
     *
     * @return 설정된 ChatClient 인스턴스.
     */
    @Bean
    public ChatClient chatClient() {
        // OpenAiChatOptions 빌더를 통해 채팅 모델의 옵션을 구성.
        // 여기서 사용할 모델("gpt-4o-mini")과 응답 생성 시의 온도(0.7)를 설정합니다.
        // 온도 값이 낮을수록 응답이 더 결정적(deterministic)이고, 높을수록 무작위성이 증가합니다.
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.7)
                .build();

        // OpenAiApi 빌더를 통해 OpenAiApi 인스턴스를 생성합니다.
        // 이 API 인스턴스는 실제 OpenAI API와의 HTTP 통신에 사용되며,
        // API 키를 사용하여 인증을 수행합니다.
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .build();

        // OpenAiChatModel 빌더를 사용하여 ChatModel 인스턴스를 생성합니다.
        // ChatModel은 OpenAiApi와 옵션을 포함하여 채팅 모델의 동작을 정의합니다.
        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)       // OpenAiApi 인스턴스를 설정하여 API 호출을 수행
                .defaultOptions(options)    // 채팅 옵션(모델, 온도 등)을 기본 옵션으로 설정
                .build();

        // 최종적으로 ChatModel을 이용하여 ChatClient를 생성합니다.
        // ChatClient는 대화 요청(prompt)과 응답 처리(call, content 등)를 위한 엔트리 포인트입니다.
        return ChatClient.builder(chatModel).build();
    }
}
