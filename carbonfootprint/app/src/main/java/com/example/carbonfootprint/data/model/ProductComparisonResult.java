package com.example.carbonfootprint.data.model;

/**
 * 상품 정보(Product)와 카테고리 평균 정보(CategoryAverage)를
 * 함께 담아서 ViewModel에서 UI로 전달하기 위한 래퍼(Wrapper) 클래스입니다.
 */
public class ProductComparisonResult {
    private final Product product;
    private final CategoryAverage categoryAverage;

    public ProductComparisonResult(Product product, CategoryAverage categoryAverage) {
        this.product = product;
        this.categoryAverage = categoryAverage;
    }

    public Product getProduct() {
        return product;
    }

    public CategoryAverage getCategoryAverage() {
        return categoryAverage;
    }
}
