package com.email.spamfilter.bloomfilter.service;

import com.email.spamfilter.bloomfilter.enums.FilterType;
import com.email.spamfilter.bloomfilter.util.BloomFilter;
import com.email.spamfilter.google.gmail.service.GmailApiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterService {
    private static final int BLOOM_FILTER_SIZE = 10000;

    private final GmailApiService gmailApiService;

    private final BloomFilter<String> trustedIpBloomFilter = new BloomFilter<>(
            BLOOM_FILTER_SIZE,
            List.of(
                    s -> s.hashCode(),
                    s -> s.hashCode() * 31,
                    s -> s.hashCode() * 17 + 7
            )
    );

    private final BloomFilter<String> spamIpBloomFilter = new BloomFilter<>(
            BLOOM_FILTER_SIZE,
            List.of(
                    s -> s.hashCode(),
                    s -> s.hashCode() * 31,
                    s -> s.hashCode() * 17 + 7
            )
    );

    private final Set<String> spamKeywords = Set.of(
            "로또", "무료", "광고", "사은품", "당첨", "긴급", "대출", "카지노", "성인", "일확천금", "배당", "투자",
            "Bitcoin", "Free", "Congratulations", "Winner", "Claim", "Urgent", "Loan", "Casino", "Adult",
            "Make money", "Click here", "Exclusive", "Offer", "Bonus"
    );

    @PostConstruct
    public void initializeIpFilters() {
        // 신뢰할 수 있는 IP 주소 목록
        List<String> trustedIps = List.of(
                "192.168.1.1", // 사설 네트워크에서 자주 사용되는 비공개 IP 주소
                "203.0.113.0", // 문서화 및 예제를 위해 예약된 IP 주소 (공용 인터넷에서 라우팅되지 않음)
                "10.0.0.1",    // 내부 네트워크에서 일반적으로 사용되는 비공개 IP 주소
                "64.233.160.0", // Google Gmail 서비스와 관련된 신뢰할 수 있는 IP 주소
                "66.249.80.0",  // Google Gmail 서비스와 관련된 신뢰할 수 있는 IP 주소
                "40.92.0.0",    // Microsoft Outlook 및 Office365에서 사용하는 신뢰할 수 있는 IP 주소
                "40.107.0.0",   // Microsoft Outlook 및 Office365에서 사용하는 신뢰할 수 있는 IP 주소
                "104.47.0.0",   // Microsoft Outlook 및 Office365에서 사용하는 신뢰할 수 있는 IP 주소
                "54.240.0.0",   // Amazon Simple Email Service(SES)에서 사용하는 신뢰할 수 있는 IP 주소
                "98.136.0.0",   // Yahoo Mail과 관련된 신뢰할 수 있는 IP 주소
                "192.0.2.1"     // 문서화 및 예제를 위해 예약된 IP 주소 (공용 인터넷에서 라우팅되지 않음)
        );

        // 신뢰할 수 없는 IP 주소 정의
        List<String> spamIps = List.of(
                "192.0.2.1", // 문서화 및 예제를 위해 예약되었지만, 실제로는 테스트 및 악의적인 활동에서 자주 사용됨
                "198.51.100.2", // 문서화 및 예제를 위해 예약되었지만, 공용 네트워크에서 종종 스팸 발송에 사용됨
                "203.0.113.5", // 문서화 및 예제를 위해 예약되었으나, 네트워크 테스트나 의심스러운 활동에서 발견됨
                "185.234.217.0", // 과거에 스팸 및 피싱 공격에 연루된 알려진 IP 주소
                "5.188.86.0", // 악성 봇네트워크 및 스팸 발송자로 자주 보고된 IP 주소
                "185.104.45.0", // 피싱 및 사기성 이메일 발송에 사용된 기록이 있는 IP 주소
                "45.134.144.0", // 악성 활동 및 사기성 행동으로 차단된 IP 주소
                "176.123.2.1", // 의심스러운 이메일 활동으로 보고된 IP 주소
                "91.200.12.0", // 악의적인 네트워크 활동과 관련된 IP 주소
                "62.210.0.0" // 다양한 스팸 활동에 연루된 데이터 센터 IP 주소
        );

        // Add to Bloom Filters
        trustedIps.forEach(trustedIpBloomFilter::add);
        spamIps.forEach(spamIpBloomFilter::add);

        log.info("신뢰할 수 있는 IP 주소와 스팸 IP 주소 목록이 초기화되었습니다.");
    }

    public List<Map<String, Object>> filterMessages(String authorizationCode, FilterType filterType) {
        List<Map<String, Object>> parsedMessages = gmailApiService.getAllMessages(authorizationCode);

        List<Map<String, Object>> filteredMessages = new ArrayList<>();
        for (Map<String, Object> messageData : parsedMessages) {
            boolean isSpam = switch (filterType) {
                case SPF -> !isValidSpf(messageData);
                case RECIEVED -> isSpamIp(messageData);
                case KEYWORDS -> containsSpamWords(messageData);
                case ALL -> !isValidSpf(messageData) || isSpamIp(messageData) || containsSpamWords(messageData);
            };

            if (isSpam) {
                messageData.put("isSpam", true);
                filteredMessages.add(messageData);
                log.warn(messageData.get("id") + "가 스팸 목록에 추가되었습니다.");

            }
        }

        return filteredMessages;
    }

    private boolean isValidSpf(Map<String, Object> message) {
        String spfHeader = extractHeader(message, "Received-SPF");
        String senderIp = extractSenderIpFromSpf(spfHeader);
        return senderIp != null && trustedIpBloomFilter.mightContain(senderIp);
    }

    private boolean isSpamIp(Map<String, Object> message) {
        String receivedHeader = extractHeader(message, "Received");
        String hostIp = extractIpFromReceivedHeader(receivedHeader);
        log.info("검사할 Received 헤더: {}, 추출된 host:{}", receivedHeader, hostIp);
        return hostIp != null && spamIpBloomFilter.mightContain(hostIp);
    }

    private boolean containsSpamWords(Map<String, Object> message) {
        // 이메일의 헤더에서 제목(Subject) 추출
        String subject = extractHeader(message, "Subject");
        log.info("검사할 제목 :{}",subject);

        if (subject == null || subject.isEmpty()) {
            log.warn("이메일 제목이 비어 있습니다.");
            return false;
        }

        // 스팸 키워드가 제목에 포함되어 있는지 검사
        return spamKeywords.stream().anyMatch(subject::contains);
    }





    private String extractHeader(Map<String, Object> message, String headerName) {
        List<Map<String, String>> headers = (List<Map<String, String>>) ((Map<String, Object>) message.get("payload")).get("headers");

        if ("Received".equalsIgnoreCase(headerName)) {
            // 가장 마지막 `Received` 헤더(즉, 메시지 전송의 최초 출발점)를 반환
            return headers.stream()
                    .filter(header -> headerName.equalsIgnoreCase(header.get("name")))
                    .map(header -> header.get("value"))
                    .reduce((first, second) -> second) // 마지막 요소를 선택
                    .orElse("");
        }

        // 다른 헤더는 기존 방식 유지
        return headers.stream()
                .filter(header -> headerName.equalsIgnoreCase(header.get("name")))
                .map(header -> header.get("value"))
                .findFirst().orElse("");
    }

    private String extractSenderIpFromSpf(String spfHeader) {
        if (spfHeader == null || spfHeader.isEmpty()) {
            log.warn("SPF 헤더가 비어 있습니다.");
            return null;
        }

        try {
            int clientIpIndex = spfHeader.indexOf("client-ip=");
            if (clientIpIndex != -1) {
                int start = clientIpIndex + "client-ip=".length();
                int end = spfHeader.indexOf(";", start);
                String clientIp = end == -1 ? spfHeader.substring(start).trim() : spfHeader.substring(start, end).trim();
                log.info("SPF 헤더에서 추출한 client-ip: {}", clientIp);
                return clientIp;
            } else {
                log.warn("SPF 헤더에서 client-ip를 찾을 수 없음: {}", spfHeader);
                return null;
            }
        } catch (Exception e) {
            log.error("SPF 헤더에서 IP 추출 중 예외 발생: {}", spfHeader, e);
            return null;
        }
    }

    private String extractIpFromReceivedHeader(String receivedHeader) {
        try {
            int ipStart = receivedHeader.indexOf("[");
            int ipEnd = receivedHeader.indexOf("]", ipStart);
            return ipStart != -1 && ipEnd != -1 ? receivedHeader.substring(ipStart + 1, ipEnd).trim() : null;
        } catch (Exception e) {
            log.warn("Received 헤더에서 IP 추출 실패: {}", receivedHeader, e);
            return null;
        }
    }
}
