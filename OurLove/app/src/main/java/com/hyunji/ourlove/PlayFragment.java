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

public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        Button btnQaAnswer = view.findViewById(R.id.btn_qa_answer);
        if (btnQaAnswer != null) {
            btnQaAnswer.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Q&A 답변 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnLetter = view.findViewById(R.id.btn_letter);
        if (btnLetter != null) {
            btnLetter.setOnClickListener(v -> {
                Toast.makeText(getContext(), "월말 편지 작성 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnCompatibility = view.findViewById(R.id.btn_compatibility);
        if (btnCompatibility != null) {
            btnCompatibility.setOnClickListener(v -> {
                Toast.makeText(getContext(), "애정 궁합 테스트 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }
}