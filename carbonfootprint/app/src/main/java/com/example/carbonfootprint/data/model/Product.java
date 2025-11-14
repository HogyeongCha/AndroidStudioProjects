package com.example.carbonfootprint.data.model;

/**
 * Firestore의 상품 문서를 담을 데이터 모델 클래스입니다.
 * Firestore가 자동으로 데이터를 매핑하려면 두 가지 규칙을 지켜야 합니다.
 * 1. public 필드 이거나, public getter가 있어야 합니다.
 * 2. 인자 없는 public 생성자(빈 생성자)가 반드시 있어야 합니다.
 */
public class Product {
    private String name;
    private Long carbonScore; // Firestore의 number 타입은 Long으로 받는 것이 안전합니다.
    private String category;

    // 1. 인자 없는 public 생성자 (필수)
    public Product() {}

    // 2. 각 필드에 대한 public getter
    public String getName() {
        return name;
    }

    public Long getCarbonScore() {
        return carbonScore;
    }

    public String getCategory() {
        return category;
    }
}

