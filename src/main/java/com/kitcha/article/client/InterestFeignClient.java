package com.kitcha.article.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "INTEREST")
public interface InterestFeignClient {

    @PostMapping("/interest")
    ResponseEntity<Map<String, String>> setInterest(@RequestBody Map<String, String> requestBody,
                                                    @RequestHeader("Authorization") String jwtToken,
                                                    @RequestHeader("X-User-Id") String userId);

}
