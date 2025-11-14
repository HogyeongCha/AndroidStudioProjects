package com.hyunji.ourlove; // 패키지 이름은 본인 환경에 맞게 수정하세요.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_play.xml 레이아웃을 인플레이트(화면에 표시)합니다.
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        // TODO: 여기에 Q&A 답변, 월말 편지, 궁합 테스트 버튼 클릭 리스너 등
        // '같이 놀기' 화면의 실제 기능 코드를 작성합니다.
        // 예: Button btnQaAnswer = view.findViewById(R.id.btn_qa_answer);
        //     btnQaAnswer.setOnClickListener(v -> { ... });

        return view;
    }
}
