package com.example.carbonfootprint.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.carbonfootprint.data.model.ProductComparisonResult;
import com.example.carbonfootprint.data.repository.ProductRepository;

public class ResultViewModel extends ViewModel {
    private final ProductRepository repository;

    public ResultViewModel() {
        repository = new ProductRepository();
    }

    public LiveData<ProductComparisonResult> getProductComparison(String gtin) {
        return repository.getProductComparison(gtin);
    }
}

