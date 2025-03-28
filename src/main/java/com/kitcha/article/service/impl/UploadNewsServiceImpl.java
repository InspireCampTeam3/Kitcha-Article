package com.kitcha.article.service.impl;

import com.kitcha.article.client.OpenAiClient;
import com.kitcha.article.dto.response.MyPickNewsResponseDto;
import com.kitcha.article.service.MyPickNewsService;
import com.kitcha.article.service.UploadNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UploadNewsServiceImpl implements UploadNewsService {

    @Autowired
    private OpenAiClient openAiClient;
    @Autowired
    private MyPickNewsService myPickNewsService;

    @Override
    public List<MyPickNewsResponseDto> processUploadedImage(MultipartFile imageFile) {
        System.out.println("[DEBUG] 업로드된 이미지 처리 시작. 파일명: " + imageFile.getOriginalFilename());

        String extractedKeyword = openAiClient.extractKeywordFromImage(imageFile);
        if (extractedKeyword == null || extractedKeyword.isBlank()) {
            System.err.println("[ERROR] 이미지에서 키워드 추출 실패");
            throw new RuntimeException("이미지에서 키워드 추출 실패");
        }

        System.out.println("[DEBUG] 추출된 키워드: " + extractedKeyword);

        // 키워드를 기반으로 뉴스 조회 (MyPickNewsService 활용)
        return myPickNewsService.getMyPickNews(extractedKeyword);
    }
}
