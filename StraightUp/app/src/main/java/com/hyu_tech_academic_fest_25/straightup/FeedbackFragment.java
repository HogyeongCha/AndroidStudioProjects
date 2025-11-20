package com.hyu_tech_academic_fest_25.straightup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// [Phase 1 추가] ViewModelProvider import
import androidx.lifecycle.ViewModelProvider;

public class FeedbackFragment extends Fragment {

    // [Phase 1 추가] ViewModel 선언
    private FeedbackViewModel feedbackViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [Phase 1 추가] ViewModel 초기화
        feedbackViewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);

        // [Phase 5에서 구현]
        // observeViewModel();
        // feedbackViewModel.generateGeminiFeedback(); // e.g., 화면 진입 시 피드백 생성
        // feedbackViewModel.loadFeedbackHistory(); // e.g., 화면 진입 시 기록 로드
    }

    // [Phase 5에서 구현]
    // private void observeViewModel() {
    //     feedbackViewModel.getGeminiCoachingText().observe(getViewLifecycleOwner(), text -> {
    //         // AI 코칭 TextView 업데이트
    //     });
    //     feedbackViewModel.getFeedbackHistory().observe(getViewLifecycleOwner(), history -> {
    //         // RecyclerView 업데이트
    //     });
    // }
}