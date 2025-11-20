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

        // [설정] 스트레칭 이미지 (이미지가 있다면 설정, 없다면 기본값 유지)
        ivStretchingPreview.setImageResource(R.drawable.img_neck_stretch);

        // 1. 최신 측정 데이터를 확인하고 AI 코칭 트리거
        dashboardViewModel.getCvaDisplayInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null && !info.cvaText.equals("-")) {
                // "53.2°" -> 53.2 파싱
                double cva = 0.0;
                try {
                    cva = Double.parseDouble(info.cvaText.replace("°", ""));
                } catch (NumberFormatException e) { e.printStackTrace(); }

                // "거북목 (Severe)" -> "Severe" 파싱 (단순화)
                String status = "Severe";
                if (info.statusLabelText.contains("Normal") || info.statusLabelText.contains("좋은")) status = "Normal";
                else if (info.statusLabelText.contains("Mild")) status = "Mild";

                // Gemini 호출 (단, 너무 잦은 호출 방지를 위해 Fragment 생성 시 1회성으로 제한하려면 플래그 필요)
                // 여기서는 데이터가 변경될 때마다 호출되므로, 실제로는 변수에 저장해두고 onViewCreated에서 한 번만 호출하는 것이 좋습니다.
                // 편의상 바로 호출합니다.
                tvGeminiFeedback.setText("Gemini가 최신 데이터(" + info.cvaText + ")를 분석 중입니다...");
                feedbackViewModel.generateCoachingFeedback(cva, status);
            } else {
                tvGeminiFeedback.setText("측정된 데이터가 없어 분석할 수 없습니다. 대시보드를 확인해주세요.");
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
}