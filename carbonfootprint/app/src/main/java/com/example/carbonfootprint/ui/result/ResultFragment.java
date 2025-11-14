package com.example.carbonfootprint.ui.result;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.carbonfootprint.R;
import com.example.carbonfootprint.data.model.Product;
import com.example.carbonfootprint.data.model.CategoryAverage;
import com.example.carbonfootprint.data.model.ProductComparisonResult;

public class ResultFragment extends Fragment {

    private static final String TAG = "ResultFragment";

    private ResultViewModel viewModel;

    // UI 요소들을 멤버 변수로 선언
    private TextView productNameTextView;
    private TextView barcodeValueTextView;
    private TextView carbonScoreTextView;
    private TextView comparisonTitleTextView;
    private ProgressBar comparisonProgressBar;
    private TextView comparisonResultTextView; // 추가된 UI
    private Button scanAgainButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ResultViewModel.class);
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        scanAgainButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ResultFragment.this).popBackStack();
        });

        if (getArguments() != null) {
            String barcode = ResultFragmentArgs.fromBundle(getArguments()).getBarcode();
            productNameTextView.setText("정보 조회중...");
            barcodeValueTextView.setText(barcode);

            Log.d(TAG, "ViewModel에 데이터 요청 시작. 바코드: " + barcode);
            observeProductData(barcode);
        }
    }

    private void initializeViews(View view) {
        productNameTextView = view.findViewById(R.id.productNameTextView);
        barcodeValueTextView = view.findViewById(R.id.barcodeValueTextView);
        carbonScoreTextView = view.findViewById(R.id.carbonScoreTextView);
        comparisonTitleTextView = view.findViewById(R.id.comparisonTitleTextView);
        comparisonProgressBar = view.findViewById(R.id.comparisonProgressBar);
        comparisonResultTextView = view.findViewById(R.id.comparisonResultTextView); // 추가된 UI 초기화
        scanAgainButton = view.findViewById(R.id.scanAgainButton);
    }

    private void observeProductData(String barcode) {
        // ViewModel 호출 메소드 변경
        viewModel.getProductComparison(barcode).observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.getProduct() != null) {
                Product product = result.getProduct();
                Toast.makeText(getContext(), product.getName() + " 정보 조회 성공!", Toast.LENGTH_SHORT).show();

                // 상품 정보 UI 업데이트
                productNameTextView.setText(product.getName());
                carbonScoreTextView.setText(product.getCarbonScore() + "점");
                comparisonTitleTextView.setText(product.getCategory() + " 카테고리 평균");

                // 카테고리 평균 정보가 있으면 비교 로직 수행
                if (result.getCategoryAverage() != null) {
                    updateComparisonUI(product, result.getCategoryAverage());
                }

            } else {
                Log.d(TAG, "데이터 수신 실패 또는 null");
                productNameTextView.setText("상품 정보를 찾을 수 없습니다.");
                Toast.makeText(getContext(), "상품 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 상품과 카테고리 평균 데이터를 비교하여 UI를 동적으로 업데이트합니다.
     */
    private void updateComparisonUI(Product product, CategoryAverage categoryAverage) {
        long myScore = product.getCarbonScore();
        long avgScore = categoryAverage.getAverageScore();

        // 프로그레스바 최대값을 평균점수의 2배로 설정 (비교를 명확하게 하기 위함)
        comparisonProgressBar.setMax((int) avgScore * 2);
        comparisonProgressBar.setProgress((int) myScore);

        // 점수 비교
        if (myScore < avgScore) {
            // 평균보다 낮음 (좋음)
            long diff = avgScore - myScore;
            double percentage = ((double) diff / avgScore) * 100;
            comparisonResultTextView.setText(String.format("평균보다 %.0f%% 낮아요!", percentage));
            comparisonResultTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary)); // 초록 계열
            comparisonProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary)));

        } else {
            // 평균보다 높거나 같음 (나쁨)
            long diff = myScore - avgScore;
            double percentage = ((double) diff / avgScore) * 100;
            comparisonResultTextView.setText(String.format("평균보다 %.0f%% 높아요.", percentage));
            comparisonResultTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_error)); // 빨강 계열
            comparisonProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.design_default_color_error)));
        }
    }
}

