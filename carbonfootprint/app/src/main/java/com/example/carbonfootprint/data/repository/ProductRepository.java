package com.example.carbonfootprint.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.carbonfootprint.data.model.CategoryAverage;
import com.example.carbonfootprint.data.model.Product;
import com.example.carbonfootprint.data.model.ProductComparisonResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductRepository {

    private static final String TAG = "ProductRepository";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final String CATEGORIES_COLLECTION = "category_averages";
    private final FirebaseFirestore db;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<ProductComparisonResult> getProductComparison(String gtin) {
        MutableLiveData<ProductComparisonResult> resultData = new MutableLiveData<>();
        Log.d(TAG, "getProductComparison() 호출됨. GTIN: " + gtin);

        // 1. 먼저 상품 정보를 가져옵니다.
        db.collection(PRODUCTS_COLLECTION).document(gtin).get().addOnCompleteListener(productTask -> {
            if (productTask.isSuccessful() && productTask.getResult().exists()) {
                Product product = productTask.getResult().toObject(Product.class);
                Log.d(TAG, "상품 정보 조회 성공: " + product.getName());

                // 2. 상품 정보에서 카테고리 이름을 얻어와 카테고리 평균 정보를 가져옵니다.
                db.collection(CATEGORIES_COLLECTION).document(product.getCategory()).get().addOnCompleteListener(categoryTask -> {
                    if (categoryTask.isSuccessful() && categoryTask.getResult().exists()) {
                        CategoryAverage categoryAverage = categoryTask.getResult().toObject(CategoryAverage.class);
                        Log.d(TAG, "카테고리 평균 조회 성공: " + categoryAverage.getAverageScore());
                        resultData.setValue(new ProductComparisonResult(product, categoryAverage));
                    } else {
                        Log.e(TAG, "카테고리 평균 조회 실패 또는 문서 없음", categoryTask.getException());
                        resultData.setValue(new ProductComparisonResult(product, null)); // 상품 정보만 반환
                    }
                });
            } else {
                Log.e(TAG, "상품 정보 조회 실패 또는 문서 없음", productTask.getException());
                resultData.setValue(null);
            }
        });

        return resultData;
    }
}

