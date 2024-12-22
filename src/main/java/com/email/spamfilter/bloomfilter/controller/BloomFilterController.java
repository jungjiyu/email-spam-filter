package com.email.spamfilter.bloomfilter.controller;


import com.email.spamfilter.bloomfilter.enums.FilterType;
import com.email.spamfilter.bloomfilter.service.BloomFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bloomfilter")
@RequiredArgsConstructor
@Slf4j
public class BloomFilterController {

    private final BloomFilterService bloomFilterService;

    /**
     * spf 레코드 분석 기반 필터링된 이메일 목록 반환 : 신뢰할 수 있는 IP 주소 목록에 존재하지 않는다면 스팸 처리
     * @param authorizationCode OAuth2 인증 코드
     * @return SPF 검증 결과가 포함된 이메일 목록
     */
    @GetMapping("/spf")
    public ResponseEntity<List<Map<String, Object>>> filterBySpf(@RequestParam("code") String authorizationCode) {
        log.info("SpamFilterController: filterBySpf 호출됨");

        try {
            List<Map<String, Object>> filteredMessages = bloomFilterService.filterMessages(authorizationCode, FilterType.SPF);
            log.info("SPF 기반 스팸 필터링 성공");
            return ResponseEntity.ok(filteredMessages);
        } catch (Exception e) {
            log.error("SPF 기반 필터링 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * recieved 헤더 기반 필터링된 이메일 목록 반환 : 가장 하단의 received 헤더의 호스트가 이 목록 중에 있는지 블룸 필터를 활용하여 빠르게 조회 및 존재한다면 스팸 처리
     * @param authorizationCode OAuth2 인증 코드
     * @return IP 검증 결과가 포함된 이메일 목록
     */
    @GetMapping("/recieved")
    public ResponseEntity<List<Map<String, Object>>> filterByRecieved(@RequestParam("code") String authorizationCode) {
        log.info("SpamFilterController: filterByIp 호출됨");

        try {
            List<Map<String, Object>> filteredMessages = bloomFilterService.filterMessages(authorizationCode, FilterType.RECIEVED);
            log.info("recieved 기반 스팸 필터링 성공");
            return ResponseEntity.ok(filteredMessages);
        } catch (Exception e) {
            log.error("recieved 기반 필터링 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 스팸 단어 기반 필터링된 이메일 목록 반환
     * @param authorizationCode OAuth2 인증 코드
     * @return 스팸 단어 포함 여부가 포함된 이메일 목록
     */
    @GetMapping("/keywords")
    public ResponseEntity<List<Map<String, Object>>> filterByKeywords(@RequestParam("code") String authorizationCode) {
        log.info("SpamFilterController: filterByKeywords 호출됨");

        try {
            List<Map<String, Object>> filteredMessages = bloomFilterService.filterMessages(authorizationCode, FilterType.KEYWORDS);
            log.info("스팸 단어 기반 필터링 성공");
            return ResponseEntity.ok(filteredMessages);
        } catch (Exception e) {
            log.error("스팸 단어 기반 필터링 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 모든 필터링 로직 적용된 이메일 목록 반환
     * @param authorizationCode OAuth2 인증 코드
     * @return 전체 필터링 결과가 포함된 이메일 목록
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> filterAll(@RequestParam("code") String authorizationCode) {
        log.info("SpamFilterController: filterAll 호출됨");

        try {
            List<Map<String, Object>> filteredMessages = bloomFilterService.filterMessages(authorizationCode, FilterType.ALL);
            log.info("전체 필터링 성공");
            return ResponseEntity.ok(filteredMessages);
        } catch (Exception e) {
            log.error("전체 필터링 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

