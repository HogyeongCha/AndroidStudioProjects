package com.hyu_tech_academic_fest_25.straightup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackFragment extends Fragment {

    private FeedbackViewModel feedbackViewModel;
    private DashboardViewModel dashboardViewModel; // 최신 측정값 가져오기 용

    private TextView tvGeminiFeedback;
    private LinearLayout layoutFeedbackHistory;
    private ImageView ivStretchingPreview;

    private final int[] IMAGES_GOOD_STATUS = {
            R.drawable.img_stretch_good_1, // 파일명에 맞게 수정하세요
            R.drawable.img_stretch_good_2
    };

    private final int[] IMAGES_BAD_STATUS = {
            R.drawable.img_stretch_bad_1,  // 파일명에 맞게 수정하세요
            R.drawable.img_stretch_bad_2
    };

    private final java.util.Random random = new java.util.Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedbackViewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);
        // Activity 범위의 ViewModel을 사용하여 측정 데이터를 공유받음
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        tvGeminiFeedback = view.findViewById(R.id.tvGeminiFeedback);
        layoutFeedbackHistory = view.findViewById(R.id.layoutFeedbackHistory);
        ivStretchingPreview = view.findViewById(R.id.ivStretchingPreview);

        // ViewModel 관찰
        dashboardViewModel.getCvaDisplayInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null && !info.cvaText.equals("-")) {
                // 1. 상태 파싱 ("거북목 (Severe)" -> "Severe")
                String status = "Severe";
                if (info.statusLabelText.contains("Normal") || info.statusLabelText.contains("좋은")) status = "Normal";
                else if (info.statusLabelText.contains("Mild")) status = "Mild";

                // 2. CVA 값 파싱
                double cva = 0.0;
                try { cva = Double.parseDouble(info.cvaText.replace("°", "")); }
                catch (Exception e) {}

                // -------------------------------------------------------
                // [통합] 뷰모델이 다 계산해준 값들을 그대로 사용
                // -------------------------------------------------------

                // (1) 이미지 업데이트 (1번 과제)
                updateStretchingGuide(status);

                // (2) 피드백 생성 (2번 과제 - info.diff 사용)
                tvGeminiFeedback.setText("Gemini가 분석 중입니다... (변화량: " + String.format("%.1f", info.diff) + ")");
                feedbackViewModel.generateCoachingFeedback(cva, status, info.diff);
            }
        });

        // 2. Gemini 결과 관찰
        feedbackViewModel.getGeminiCoachingText().observe(getViewLifecycleOwner(), text -> {
            tvGeminiFeedback.setText(text);
        });

        // 3. 과거 기록 관찰 및 UI 동적 생성
        feedbackViewModel.getFeedbackHistory().observe(getViewLifecycleOwner(), this::updateHistoryList);
    }

    private void updateHistoryList(List<UserFeedback> list) {
        layoutFeedbackHistory.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

        for (UserFeedback item : list) {
            // 기록 아이템 뷰 생성 (CardView로 감싸서 예쁘게)
            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setCardBackgroundColor(Color.WHITE);
            card.setRadius(12f);
            card.setCardElevation(1f);

            LinearLayout innerLayout = new LinearLayout(requireContext());
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            innerLayout.setPadding(32, 24, 32, 24);

            // 날짜
            TextView tvDate = new TextView(requireContext());
            tvDate.setText(sdf.format(new Date(item.getTimestamp())));
            tvDate.setTextSize(12);
            tvDate.setTextColor(Color.GRAY);
            innerLayout.addView(tvDate);

            // 내용
            TextView tvContent = new TextView(requireContext());
            tvContent.setText(item.getFeedbackText());
            tvContent.setTextSize(14);
            tvContent.setTextColor(Color.DKGRAY);
            tvContent.setPadding(0, 8, 0, 0);
            innerLayout.addView(tvContent);

            card.addView(innerLayout);
            layoutFeedbackHistory.addView(card);
        }
    }
    /**
     * [추가] 현재 목 상태(Normal/Mild/Severe)에 따라
     * 적절한 스트레칭 이미지를 랜덤하게 선택하여 보여주는 메서드
     */
    private void updateStretchingGuide(String status) {
        int[] targetArray;

        // 1. 상태에 따라 사용할 이미지 배열 선택
        if ("Normal".equals(status) || "좋은 자세".equals(status)) {
            targetArray = IMAGES_GOOD_STATUS;
        } else {
            // Mild, Severe 등 거북목 의심 증상이 있을 때
            targetArray = IMAGES_BAD_STATUS;
        }

        // 2. 배열이 비어있지 않다면 랜덤하게 하나 뽑기
        if (targetArray.length > 0) {
            int randomIndex = random.nextInt(targetArray.length);
            int selectedImageResId = targetArray[randomIndex];

            // 3. 이미지 뷰에 적용
            ivStretchingPreview.setImageResource(selectedImageResId);
        }
    }
}