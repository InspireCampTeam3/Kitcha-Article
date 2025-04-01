package com.kitcha.article.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InterestServiceClient {

    @Autowired
    private InterestFeignClient interestFeignClient;

    public void setInterest(String interest, HttpHeaders headers) {
        // 헤더에서 사용자 ID와 JWT 토큰 가져오기
        if (!headers.containsKey("X-User-Id") || !headers.containsKey("Authorization")) {
            throw new IllegalArgumentException("헤더에 X-User-Email 또는 Authorization이 누락되었습니다.");
        }
        String userId = headers.getFirst("X-User-Id");  // 사용자 ID
        String jwtToken = headers.getFirst("Authorization");  // JWT 토큰


        // 관심사만 전달
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("interest", interest);

        // 디버깅 로그
        System.out.println("🚀 [API 요청] 관심사 전달 시작");
        System.out.println("🌐 요청 URL: /interest");
        System.out.println("🔑 X-User-Id: " + userId);
        System.out.println("🔐 JWT Token: " + jwtToken);
        System.out.println("📦 요청 본문: " + requestBody);

        // Feign 클라이언트를 사용한 다른 마이크로 서비스 API 호출
        try {
            ResponseEntity<Map<String, String>> response = interestFeignClient.setInterest(requestBody, jwtToken, userId);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("관심사 등록 성공: " + response.getBody());
            } else {
                System.out.println("관심사 등록 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
        }
    }
}
