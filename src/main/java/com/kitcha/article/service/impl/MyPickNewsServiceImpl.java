package com.kitcha.article.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.kitcha.article.client.ArticleCrawler;
import com.kitcha.article.client.OpenAiClient;
import com.kitcha.article.client.NaverApiClient;
import com.kitcha.article.dto.response.MyPickNewsResponseDto;
import com.kitcha.article.service.MyPickNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class MyPickNewsServiceImpl implements MyPickNewsService {

    @Autowired
    private NaverApiClient naverApiClient;
    @Autowired
    private ArticleCrawler articleCrawler;
    @Autowired
    private OpenAiClient openAiClient;

    private final Random random = new Random();

    @Override
    public List<MyPickNewsResponseDto> getMyPickNews(String interest) {

        // 입력 값 기반 검색
        JsonNode items = naverApiClient.fetchNews(interest, 20);
        List<JsonNode> allNewsList = new ArrayList<>();

        // JSON 데이터를 리스트에 담기
        items.forEach(allNewsList::add);

        // 무작위로 5개 추출
        List<JsonNode> randomNewsList = allNewsList.stream()
                .sorted((a, b) -> random.nextInt(3) - 1) // 랜덤 섞기
                .limit(5) // 5개 선택
                .collect(Collectors.toList());

        List<MyPickNewsResponseDto> newsList = new ArrayList<>();

        // 4. 추출된 뉴스에 대해 요약 수행
        for (JsonNode item : randomNewsList) {
            String newsTitle = item.path("title").asText().replaceAll("<.*?>", "");  // HTML 태그 제거
            String newsDate = item.path("pubDate").asText();
            String newsUrl = item.path("link").asText();
            String shortSummary = item.path("description").asText().replaceAll("<.*?>", "");

            // 기사 본문 크롤링
            String articleContent = articleCrawler.getArticleContent(newsUrl);

            // Groq API 요약
            Map<String, String> summaries = openAiClient.getArticleSummaries(articleContent);
            String longSummary = summaries.getOrDefault("longSummary", "요약 실패");

            // DTO 생성 및 추가
            MyPickNewsResponseDto dto = new MyPickNewsResponseDto(
                    newsTitle,
                    newsDate,
                    newsUrl,
                    shortSummary,
                    longSummary
            );
            newsList.add(dto);
        }
        return newsList;
    }
}

