package com.hyunji.ourlove;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AnalysisFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        Button btnReportGo = view.findViewById(R.id.btn_report_go);
        if (btnReportGo != null) {
            btnReportGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "월간 리포트 확인 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnRecommendGo = view.findViewById(R.id.btn_recommend_go);
        if (btnRecommendGo != null) {
            btnRecommendGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "데이트 코스 추천받기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnCounselingGo = view.findViewById(R.id.btn_counseling_go);
        if (btnCounselingGo != null) {
            btnCounselingGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "상담 시작하기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }
}