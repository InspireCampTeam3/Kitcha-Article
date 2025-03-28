package com.kitcha.article.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.kitcha.article.client.ArticleCrawler;
import com.kitcha.article.client.OpenAiClient;
import com.kitcha.article.client.NaverApiClient;
import com.kitcha.article.dto.response.RandomNewsResponseDto;
import com.kitcha.article.service.RandomNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class RandomNewsServiceImpl implements RandomNewsService {

    @Autowired
    private NaverApiClient naverApiClient;
    @Autowired
    private ArticleCrawler articleCrawler;
    @Autowired
    private OpenAiClient openAiClient;

    private static final String[] INTERESTS =
            {"드라마", "엔터", "뮤직", "영화",
            "정치", "경제", "사회", "생활",
            "주식", "문화", "IT/과학", "세계",
            "야구", "축구", "농구", "아시안게임"};

    @Override
    public RandomNewsResponseDto getRandomNews() {
        // 랜덤 관심사 키워드 선택
        String randomInterest = INTERESTS[new Random().nextInt(INTERESTS.length)];

        // Naver API를 통해 랜덤 키워드 뉴스 검색
        JsonNode items = naverApiClient.fetchNews(randomInterest, 1);
        if (items.isEmpty()) {
            throw new RuntimeException("랜덤 뉴스 검색 실패");
        }

        // 첫 번째 기사 추출
        JsonNode item = items.get(0);
        String newsTitle = item.path("title").asText().replaceAll("<.*?>", "");
        String newsUrl = item.path("link").asText();

        // 기사 본문 크롤링
        String articleContent = articleCrawler.getArticleContent(newsUrl);

        // 5. Groq API를 통해 기사 요약
        Map<String, String> summaries = openAiClient.getArticleSummaries(articleContent);
        String longSummary = summaries.getOrDefault("longSummary", "요약 실패");

        // 6. 핵심 키워드 추출
        String keyword = openAiClient.extractKeyword(longSummary);

        // 7. DTO 생성 및 반환
        return new RandomNewsResponseDto(newsTitle,longSummary,randomInterest,keyword);
    }
}
