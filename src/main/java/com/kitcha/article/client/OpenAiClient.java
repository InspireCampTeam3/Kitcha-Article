package com.kitcha.article.client;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class OpenAiClient {
    private final ChatClient chatClient;

    // 공통 요청 메서드
    private String sendRequest(String systemPrompt, String userPrompt) {
        return handleWithAiError(() -> chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content()
        );
    }

    private String sendRequest(String systemPrompt, MimeType mimeType, ByteArrayResource imageResource) {
        return handleWithAiError(() -> chatClient
                .prompt()
                .system(systemPrompt)
                .user(u -> u.text("Explain what do you see on this picture?")
                        .media(mimeType, imageResource)) // Consumer<UserMessageBuilder>
                .call()
                .content()
        );
    }

    // 기사 요약 요청 결과 데이터 파싱
    public Map<String, String> getArticleSummaries(String content) {
        Map<String, String> summaries = new HashMap<>();
        summaries.put("longSummary", requestSummary(content));
        return summaries;
    }

    // 기사 요약 요청
    private String requestSummary(String content) {
        String systemPrompt =
                """
                    당신은 뉴스 기사를 간결하게 요약해주는 전문가입니다. 사용자가 제공한 뉴스 기사의 핵심 내용을 파악하고, 독자가 빠르게 이해할 수 있도록 요약해 주세요.
                    
                    - 요약은 반드시 **한글로 작성**해야 합니다.
                    - 총 **8문장으로 요약**해 주세요. 문장은 짧고 명확하게 써주세요.
                    - 핵심 인물, 사건, 시간, 장소, 원인, 결과가 포함되도록 정리해주세요.
                    - 뉴스 기사 전체 흐름이 자연스럽게 이어지도록 문장을 구성해주세요.
                    - 숫자나 고유명사 등은 가능한 정확하게 포함해주세요.
                    - 추가적인 추론이나 해석은 하지 마세요. 기사에 나온 정보만 요약해주세요.
                """;

        String userPromptTemplate =
                """
                    입력 기사:
                    %s
                """;

        // 사용자 프롬프트 템플릿에 실제 뉴스 기사 내용을 포맷팅하여 삽입
        String userPrompt = String.format(userPromptTemplate, content);

        return sendRequest(systemPrompt, userPrompt);
    }

    // 텍스트 핵심 키워드 추출
    public String extractKeyword(String content) {
        String systemPrompt =
                """
                    당신은 뉴스 기사의 내용을 분석하여 핵심 키워드를 한 단어로 요약하는 전문가입니다.
                        
                    - 사용자가 제공한 뉴스 기사 내용을 분석하고, 핵심 주제나 개념을 가장 잘 나타내는 **한글 단어 하나**를 추출해 주세요.
                    - 키워드는 명사형 단어로, 기사 전체를 대표할 수 있는 핵심어여야 합니다.
                    - 너무 일반적인 단어(예: 기사, 사건, 내용, 문제 등)는 피하고, 구체적이고 핵심적인 단어를 선택해 주세요.
                    - 예를 들어, 기후 변화 기사면 ‘탄소중립’, 부동산 정책 기사면 ‘공시지가’, 인공지능 기사면 ‘챗봇’처럼 주요 주제를 한 단어로 요약해 주세요.
                    - 결과는 반드시 **한 단어만 출력**해. 다른 설명이나 문장은 포함하지 마세요.
                """;
        String userPromptTemplate =
                """
                    입력 기사:
                    %s
                """;

        String userPrompt = String.format(userPromptTemplate, content);

        // 사용자 프롬프트 템플릿에 실제 뉴스 기사 내용을 포맷팅하여 삽입

        return sendRequest(systemPrompt, userPrompt);
    }

    // 이미지 속 키워드 추출
    public String extractKeywordFromImage(MultipartFile imageFile) {
        try {
            String systemPrompt =
                    """
                        너는 이미지를 보고 핵심 의미를 파악하여 가장 관련 있는 한글 단어 하나를 반환하는 전문가입니다.
                            
                        - 사용자가 제공한 이미지를 분석하고, 그 이미지의 주제나 의미를 가장 잘 나타내는 **한글 단어 하나만** 반환해 주세요.
                        - **설명, 이유, 해석, 문장 등은 절대 쓰지 마세요. 단어만 출력하세요.**
                        - 단어는 **한글**이어야 하며, 너무 일반적이지 않고 이미지와 직접적으로 연관된 것이어야 합니다.
                        - 예를 들어, 음식 사진이면 ‘피자’, 바다 사진이면 ‘해변’, 그래프 이미지면 ‘통계’처럼 해당 이미지를 가장 잘 설명하는 **구체적인 단어 하나**를 선택해 주세요.
                        - 결과는 오직 **단어 하나**여야 합니다.
                    """;

            MimeType mimeType = MimeTypeUtils.parseMimeType(Objects.requireNonNull(imageFile.getContentType()));
            // MultipartFile → ByteArrayResource
            ByteArrayResource imageResource = new ByteArrayResource(imageFile.getBytes());

            return sendRequest(systemPrompt, mimeType, imageResource);
        } catch (IOException e) {
            System.err.println("이미지 처리 실패: " + e.getMessage());
            return "키워드 추출 실패: 이미지 처리 중 오류 발생";
        }
    }

    private String handleWithAiError(Supplier<String> action) {
        try {
            return action.get();
        } catch (IllegalArgumentException e) {
            System.err.println("[ChatGPTService] 잘못된 입력 값: " + e.getMessage());
            return "입력값에 문제가 있습니다. 다시 시도해주세요.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("[ChatGPTService] 클라이언트 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "요청이 잘못되었습니다. 관리자에게 문의해주세요.";

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            System.err.println("[ChatGPTService] 서버 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "OpenAI 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";

        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("[ChatGPTService] 네트워크 연결 실패: " + e.getMessage());
            return "네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.";

        } catch (Exception e) {
            System.err.println("[ChatGPTService] 알 수 없는 오류: " + e.getMessage());
            e.printStackTrace();
            return "요약 중 알 수 없는 문제가 발생했습니다. 다시 시도해주세요.";
        }
    }

}
