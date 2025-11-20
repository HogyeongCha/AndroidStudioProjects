package com.hyu_tech_academic_fest_25.straightup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;

public class FeedbackFragment extends Fragment {

    private FeedbackViewModel feedbackViewModel;
    private DashboardViewModel dashboardViewModel;
    private SettingsViewModel settingsViewModel; // 설정 확인용

    private TextView tvGeminiFeedback;

    // Nano-Banana UI
    private MaterialCardView cardNanoBanana;
    private Button btnGenerateNanoBanana;
    private ImageView ivNanoBanana;
    private ProgressBar pbNanoBanana;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel 초기화 (Activity 범위로 공유하여 데이터 유지)
        feedbackViewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        // View Binding
        tvGeminiFeedback = view.findViewById(R.id.tvGeminiFeedback);

        // Nano-Banana UI 초기화
        cardNanoBanana = view.findViewById(R.id.cardNanoBanana);
        btnGenerateNanoBanana = view.findViewById(R.id.btnGenerateNanoBanana);
        ivNanoBanana = view.findViewById(R.id.ivNanoBanana);
        pbNanoBanana = view.findViewById(R.id.pbNanoBanana);

        setupNanoBanana();
        observeViewModels();

        // 초기 피드백 요청 (데이터가 있는 경우에만)
        // 여기서는 간단히 테스트용 호출 (데이터가 없어도 동작 확인)
        // 실제로는 DashboardViewModel에서 최신 데이터를 가져와야 함
        // feedbackViewModel.generateCoachingFeedback(45.0f, "Mild");
    }

    private void setupNanoBanana() {
        btnGenerateNanoBanana.setOnClickListener(v -> {
            if (CameraViewModel.lastCapturedBitmap == null) {
                Toast.makeText(getContext(), "아직 캡처된 이미지가 없습니다. 모니터링 탭에서 측정을 먼저 진행해주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            feedbackViewModel.generateNanoBananaSimulation();
            pbNanoBanana.setVisibility(View.VISIBLE);
            ivNanoBanana.setVisibility(View.GONE);
            btnGenerateNanoBanana.setEnabled(false);
            btnGenerateNanoBanana.setText("생성 중...");
        });
    }

    private void observeViewModels() {
        // 1. AI 코칭 텍스트 관찰
        feedbackViewModel.getGeminiCoachingText().observe(getViewLifecycleOwner(), text -> {
            if (text != null) {
                tvGeminiFeedback.setText(text);
            }
        });

        // 2. Nano-Banana 설정 확인 (설정에서 끄면 카드 숨김)
        settingsViewModel.getIsNanoBananaEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            if (isEnabled) {
                cardNanoBanana.setVisibility(View.VISIBLE);
            } else {
                cardNanoBanana.setVisibility(View.GONE);
            }
        });

        // 3. Nano-Banana 결과 (텍스트 묘사)
        feedbackViewModel.getNanoBananaDescription().observe(getViewLifecycleOwner(), description -> {
            // 텍스트 묘사는 Toast로 보여주거나 별도 뷰에 표시할 수 있음
            if (description != null && !description.isEmpty()) {
                // Toast.makeText(getContext(), description, Toast.LENGTH_LONG).show();
            }
        });

        // 4. Nano-Banana 결과 (교정된 이미지)
        feedbackViewModel.getNanoBananaImage().observe(getViewLifecycleOwner(), bitmap -> {
            pbNanoBanana.setVisibility(View.GONE);
            btnGenerateNanoBanana.setEnabled(true);
            btnGenerateNanoBanana.setText("다시 생성하기");

            if (bitmap != null) {
                ivNanoBanana.setVisibility(View.VISIBLE);
                ivNanoBanana.setImageBitmap(bitmap); // 생성된 비트맵 설정!
                Toast.makeText(getContext(), "자세 교정 시뮬레이션이 완료되었습니다!", Toast.LENGTH_SHORT).show();
            } else {
                // 이미지가 null인 경우 (캡처된 원본이 없거나 오류 발생)
                // Toast.makeText(getContext(), "이미지 생성 실패", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. 로딩 상태 관찰 (공통)
        feedbackViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // 필요한 경우 전체 화면 로딩 표시 등을 처리
        });
    }
}