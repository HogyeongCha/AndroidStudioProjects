package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * FeedbackFragment의 UI 상태와 비즈니스 로직(Gemini API)을 관리할 ViewModel
 * (Phase 1에서는 Stub)
 */
public class FeedbackViewModel extends AndroidViewModel {

    // (Phase 5에서 구현)
    // private MutableLiveData<String> geminiCoachingText = new MutableLiveData<>();
    // private MutableLiveData<List<UserFeedback>> feedbackHistory = new MutableLiveData<>();

    public FeedbackViewModel(@NonNull Application application) {
        super(application);
    }

    // (Phase 5에서 구현)
    // public void generateGeminiFeedback() {
    //     // 1. Firebase에서 최근 CVA 데이터 요약
    //     // 2. Gemini API 호출
    //     // 3. geminiCoachingText LiveData 업데이트
    //     // 4. Firebase에 UserFeedback 저장
    // }
    //
    // public void loadFeedbackHistory() {
    //     // 1. Firebase에서 feedbackHistory 로드
    //     // 2. feedbackHistory LiveData 업데이트
    // }
}