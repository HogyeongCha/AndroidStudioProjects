package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Gemini SDK
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

// Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FeedbackViewModel extends AndroidViewModel {

    private static final String TAG = "FeedbackViewModel";

    // BuildConfig에서 API 키 가져오기 (환경변수/local.properties 활용)
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private final GenerativeModelFutures model;
    private final MutableLiveData<String> geminiCoachingText = new MutableLiveData<>();
    private final MutableLiveData<String> nanoBananaDescription = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> nanoBananaImage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final Executor executor = Executors.newSingleThreadExecutor();

    public FeedbackViewModel(@NonNull Application application) {
        super(application);

        // API 키가 없는 경우 로그 경고 (개발 편의성)
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("\"\"")) {
            Log.e(TAG, "GEMINI_API_KEY가 설정되지 않았습니다. local.properties를 확인해주세요.");
        }

        // Gemini 1.5 Flash 모델 초기화
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        model = GenerativeModelFutures.from(gm);
    }

    public LiveData<String> getGeminiCoachingText() {
        return geminiCoachingText;
    }

    public LiveData<String> getNanoBananaDescription() {
        return nanoBananaDescription;
    }

    public LiveData<Bitmap> getNanoBananaImage() {
        return nanoBananaImage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * 최근 CVA 데이터 통계를 바탕으로 AI 피드백을 요청합니다.
     * @param averageCva 최근 평균 CVA
     * @param worstStatus 가장 안 좋았던 상태 (Severe/Mild/Normal)
     */
    public void generateCoachingFeedback(float averageCva, String worstStatus) {
        if (averageCva == 0.0f) {
            geminiCoachingText.setValue("아직 충분한 데이터가 수집되지 않았습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        isLoading.setValue(true);

        String prompt = String.format(
                "당신은 전문 자세 교정 물리치료사입니다.\n" +
                        "사용자의 최근 자세 분석 데이터는 다음과 같습니다:\n" +
                        "- 평균 두개척추각(CVA): %.1f도\n" +
                        "- 주요 상태: %s\n\n" +
                        "이 데이터를 바탕으로 사용자에게 3줄 이내로 친근하게 현재 상태를 설명하고, " +
                        "지금 당장 할 수 있는 간단한 스트레칭이나 자세 교정 팁을 한 가지 추천해 주세요. " +
                        "전문 용어보다는 쉬운 말을 사용해 주세요.",
                averageCva, worstStatus
        );

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String feedback = result.getText();
                geminiCoachingText.postValue(feedback);
                isLoading.postValue(false);

                saveFeedbackToFirebase(feedback);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini API 호출 실패", t);
                geminiCoachingText.postValue("AI 코칭을 불러오는 데 실패했습니다. API 키나 네트워크 상태를 확인해주세요.");
                isLoading.postValue(false);
            }
        }, executor);
    }

    /**
     * Nano-Banana 시뮬레이션 생성
     * 1. Gemini에게 긍정적인 미래 묘사 요청 (텍스트)
     * 2. PostureCorrector를 사용해 실제 이미지 교정 수행 (이미지)
     */
    public void generateNanoBananaSimulation() {
        isLoading.setValue(true);

        // 1. Gemini 텍스트 생성 요청
        String prompt = "당신은 긍정적인 동기부여가입니다. " +
                "사용자가 거북목을 교정하고 바른 자세를 가졌을 때, " +
                "10년 후 얼마나 건강하고 자신감 넘치는 모습일지" +
                "바른 자세를 하고 있는 사람의 모습";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String description = result.getText();
                nanoBananaDescription.postValue(description);

                // 2. 실제 이미지 워핑 수행 (백그라운드 스레드)
                executor.execute(() -> {
                    Bitmap original = CameraViewModel.lastCapturedBitmap;
                    List<NormalizedLandmark> landmarks = CameraViewModel.lastLandmarks;

                    if (original != null && landmarks != null) {
                        // PostureCorrector를 사용하여 이미지 변형
                        Bitmap corrected = PostureCorrector.createCorrectedImage(original, landmarks);
                        nanoBananaImage.postValue(corrected);
                    } else {
                        Log.w(TAG, "캡처된 이미지가 없어 시뮬레이션을 수행할 수 없습니다.");
                    }
                    isLoading.postValue(false);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Nano-Banana 생성 실패", t);
                nanoBananaDescription.postValue("시뮬레이션 생성에 실패했습니다.");
                isLoading.postValue(false);
            }
        }, executor);
    }

    private void saveFeedbackToFirebase(String feedback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("feedbackHistory");

            UserFeedback record = new UserFeedback(System.currentTimeMillis(), feedback);
            dbRef.push().setValue(record);
        }
    }
}