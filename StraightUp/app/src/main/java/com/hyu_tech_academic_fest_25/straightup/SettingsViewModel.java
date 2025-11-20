package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * SettingsFragment의 UI 상태와 비즈니스 로직(SharedPreferences)을 관리할 ViewModel
 * (Phase 1에서는 Stub)
 */
public class SettingsViewModel extends AndroidViewModel {

    // (Phase 6에서 구현)
    // private SharedPreferences prefs;
    // private MutableLiveData<String> alertMethod = new MutableLiveData<>();
    // private MutableLiveData<Integer> captureInterval = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        // prefs = ... (SharedPreferences 초기화)
    }

    // (Phase 6에서 구현)
    // public void setAlertMethod(String method) {
    //     // 1. SharedPreferences에 저장
    //     // 2. alertMethod LiveData 업데이트
    // }
    //
    // public void setCaptureInterval(int seconds) {
    //     // ...
    // }
}