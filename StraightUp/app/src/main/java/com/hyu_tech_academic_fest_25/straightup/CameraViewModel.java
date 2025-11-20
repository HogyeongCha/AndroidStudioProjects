package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import android.graphics.Bitmap; // [필수] Bitmap import 확인
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark; // [필수]
import java.util.List; // [필수]

// Firebase 관련 import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CameraViewModel extends AndroidViewModel {

    private MutableLiveData<CvaDataPoint> latestCva = new MutableLiveData<>();
    private MutableLiveData<String> ttsAlert = new MutableLiveData<>();

    // [핵심] 이 부분이 누락되어 오류가 발생했습니다!
    // Nano-Banana 시뮬레이션을 위해 캡처된 이미지를 임시로 저장하는 변수입니다.
    public static Bitmap lastCapturedBitmap = null;
    public static List<NormalizedLandmark> lastLandmarks = null;

    private static final long ALERT_INTERVAL_MS = 30000;
    private long lastAlertTimestamp = 0;
    private static final long SAVE_INTERVAL_MS = 5000;
    private long lastSaveTimestamp = 0;

    public CameraViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<CvaDataPoint> getLatestCva() { return latestCva; }
    public LiveData<String> getTtsAlert() { return ttsAlert; }

    // [수정] Bitmap 인자 추가 및 저장 로직
    public void processImageAnalysisResult(AnalysisResult result, Bitmap originalBitmap) {
        if (result == null || result.classification.equals("Analyzing...")) {
            return;
        }

        // [핵심] 나쁜 자세일 때(또는 이미지가 없을 때) 시뮬레이션용으로 비트맵 저장
        if (result.classification.equals("Severe") || lastCapturedBitmap == null) {
            lastCapturedBitmap = originalBitmap; // 여기서 이미지를 저장합니다.
            lastLandmarks = result.landmarks;
        }

        long currentTime = System.currentTimeMillis();
        CvaDataPoint dataPoint = new CvaDataPoint(currentTime, result.cva, result.classification);
        latestCva.setValue(dataPoint);

        if (result.cva != 0.0 && currentTime - lastSaveTimestamp >= SAVE_INTERVAL_MS) {
            saveToFirebase(dataPoint);
            lastSaveTimestamp = currentTime;
        }

        if (result.cva != 0.0 && currentTime - lastAlertTimestamp >= ALERT_INTERVAL_MS) {
            if (result.classification.equals("Severe")) {
                ttsAlert.setValue("자세가 매우 좋지 않습니다. (" + result.classification + ") 목을 뒤로 당겨주세요!");
                lastAlertTimestamp = currentTime;
            } else if (result.classification.equals("Mild")) {
                ttsAlert.setValue("자세가 흐트러지고 있습니다. (" + result.classification + ") 허리를 펴주세요.");
                lastAlertTimestamp = currentTime;
            }
        }
    }

    public void clearTtsAlert() { ttsAlert.setValue(null); }

    private void saveToFirebase(CvaDataPoint data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("cvaHistory");
            dbRef.push().setValue(data);
        }
    }
}