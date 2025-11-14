package com.example.carbonfootprint.data.model;

/**
 * Firestore의 'category_averages' 컬렉션 문서를 담을 데이터 모델입니다.
 */
public class CategoryAverage {
    private Long averageScore;

    public CategoryAverage() {}

    public Long getAverageScore() {
        return averageScore;
    }
}
