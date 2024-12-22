package com.email.spamfilter.bloomfilter.util;

import lombok.Builder;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

public class BloomFilter<T> {
    private final BitSet bitSet;
    private final int size;
    private final List<Function<T, Integer>> hashFunctions;

    /**
     * 블룸 필터 초기화
     * @param size 비트 배열 크기
     * @param hashFunctions 해시 함수 리스트
     */
    public BloomFilter(int size, List<Function<T, Integer>> hashFunctions) {
        this.size = size;
        this.hashFunctions = hashFunctions;
        this.bitSet = new BitSet(size);
    }

    /**
     * 요소 추가
     * @param value 추가할 요소
     */
    public void add(T value) {
        for (Function<T, Integer> hashFunction : hashFunctions) {
            int hash = Math.abs(hashFunction.apply(value)) % size; // 비트 배열 크기(size)로 모듈러 연산을 수행하여 비트 배열의 인덱스를 계산
            bitSet.set(hash);
        }
    }

    /**
     * 요소 존재 (가능성) 여부 확인.
     * @param value 확인할 요소
     * @return 존재 여부
     */
    public boolean mightContain(T value) {
        for (Function<T, Integer> hashFunction : hashFunctions) { // hashFunctions 리스트에 포함된 해시 함수들을 순차적으로 실행
            int hash = Math.abs(hashFunction.apply(value)) % size; // 각 해시 함수로 해당 값에 대한 인덱스 계산
            if (!bitSet.get(hash)) {
                return false; //하나라도 false인 비트가 있다면, 요소는 필터에 존재하지 않음
            }
        }
        return true; // 모든 비트가 true -> 해당 요소가 블룸 필터에 존재할 가능성이 있음.
    }
}

