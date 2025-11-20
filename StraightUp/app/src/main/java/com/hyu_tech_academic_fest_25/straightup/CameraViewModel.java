package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * CameraFragment의 UI 상태와 비즈니스 로직(MediaPipe, TFLite)을 관리할 ViewModel
 * (Phase 1에서는 Stub)
 */
public class CameraViewModel extends AndroidViewModel {

    // (Phase 2에서 구현)
    // private MutableLiveData<CvaDataPoint> latestCva = new MutableLiveData<>();
    // private MutableLiveData<String> ttsAlert = new MutableLiveData<>();

    public CameraViewModel(@NonNull Application application) {
        super(application);
    }

    // (Phase 2에서 구현)
    // public void processImage(ImageProxy image) {
    //     // 1. MediaPipe로 랜드마크 추출 -> CVA 계산
    //     // 2. TFLite로 이미지 분류
    //     // 3. latestCva LiveData 업데이트
    //     // 4. Firebase에 CvaDataPoint 저장
    //     // 5. ttsAlert LiveData 업데이트 (필요시)
    // }
}