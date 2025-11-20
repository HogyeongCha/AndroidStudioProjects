package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FeedbackViewModel extends AndroidViewModel {

    private final MutableLiveData<String> geminiCoachingText = new MutableLiveData<>();
    private final MutableLiveData<List<UserFeedback>> feedbackHistory = new MutableLiveData<>();

    // [핵심] Gemini API 관련 코드 삭제됨

    public FeedbackViewModel(@NonNull Application application) {
        super(application);
        loadFeedbackHistory();
    }

    public LiveData<String> getGeminiCoachingText() { return geminiCoachingText; }
    public LiveData<List<UserFeedback>> getFeedbackHistory() { return feedbackHistory; }

    /**
     * [수정] 실제 AI 호출 대신, 미리 준비된 문장을 반환하는 "가짜(Mock)" 메서드
     * 1.5초 딜레이를 주어 실제 분석하는 듯한 느낌을 줍니다.
     */
    public void generateCoachingFeedback(double cva, String classification) {
        // 로딩 중 메시지는 Fragment에서 이미 설정했으므로, 여기선 결과만 주면 됨

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String fakeAiResponse = getPredefinedMessage(cva, classification);
            geminiCoachingText.setValue(fakeAiResponse);

            // 기록 저장은 동일하게 수행 (발표 시 기록이 쌓이는 것을 보여주기 위함)
            saveFeedbackToFirebase(fakeAiResponse);
        }, 1500); // 1.5초 딜레이 (AI가 생각하는 척)
    }

    /**
     * 상태별 미리 준비된 "AI스러운" 멘트 생성기
     */
    private String getPredefinedMessage(double cva, String classification) {
        Random random = new Random();
        String[] messages;

        switch (classification) {
            case "Normal":
                messages = new String[] {
                        String.format("현재 목 각도는 %.1f도로 아주 훌륭합니다! 지금처럼 턱을 가볍게 당긴 자세를 유지하세요.", cva),
                        "완벽한 자세입니다. 현재 상태를 유지하며 30분마다 가볍게 어깨를 돌려주는 것으로 충분합니다.",
                        "목과 어깨의 정렬이 바릅니다. 좋은 습관이 몸에 배어 있군요! 계속 유지해주세요."
                };
                break;
            case "Mild":
                messages = new String[] {
                        String.format("목 각도가 %.1f도로 주의가 필요합니다. 시선을 10도만 높이고 어깨를 펴주세요.", cva),
                        "자세가 조금씩 무너지고 있습니다. 턱을 쇄골 쪽으로 살짝 당기고 허리를 곧게 펴보세요.",
                        "경미한 거북목 증상이 보입니다. 스마트폰을 눈높이까지 들어 올리는 습관을 들여보세요."
                };
                break;
            case "Severe":
            default:
                messages = new String[] {
                        String.format("경고: 목 각도가 %.1f도로 심각합니다. 즉시 하던 일을 멈추고 목 신전 운동을 하세요!", cva),
                        "목에 가해지는 하중이 매우 큽니다(Severe). 지금 바로 아래의 스트레칭을 따라 하여 근육을 이완시켜주세요.",
                        "위험한 자세가 지속되고 있습니다. 통증이 생기기 전에 턱을 뒤로 당겨 귀와 어깨 라인을 맞춰주세요."
                };
                break;
        }

        // 랜덤으로 하나 선택해서 매번 조금 다른 척 함
        return messages[random.nextInt(messages.length)];
    }

    private void saveFeedbackToFirebase(String text) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("feedbackHistory");

        UserFeedback feedback = new UserFeedback(System.currentTimeMillis(), text);
        ref.push().setValue(feedback);
    }

    private void loadFeedbackHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("feedbackHistory");

        ref.limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserFeedback> list = new ArrayList<>();
                for (DataSnapshot post : snapshot.getChildren()) {
                    UserFeedback item = post.getValue(UserFeedback.class);
                    if (item != null) list.add(item);
                }

                if (list.isEmpty()) {
                    generateMockHistoryData();
                } else {
                    Collections.reverse(list);
                    if (list.size() > 3) list = list.subList(0, 3);
                    feedbackHistory.setValue(list);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void generateMockHistoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("feedbackHistory");

        long now = System.currentTimeMillis();

        ref.push().setValue(new UserFeedback(now - 3600000 * 2,
                "목을 5도 정도 더 뒤로 당겨보세요. 아주 조금만 더 노력하면 정상 범위입니다!"));
        ref.push().setValue(new UserFeedback(now - 3600000 * 4,
                "장시간 고정된 자세는 위험합니다. 지금 바로 스트레칭을 진행해주세요."));
        ref.push().setValue(new UserFeedback(now - 3600000 * 24,
                "자세가 아주 좋습니다! 현재 상태를 10분간 유지해보세요."));
    }
}