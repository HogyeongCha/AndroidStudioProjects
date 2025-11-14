package com.hyunji.ourlove; // 패키지 이름은 본인 환경에 맞게 수정하세요.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_home.xml 레이아웃을 인플레이트(화면에 표시)합니다.
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // TODO: 여기에 D-DAY 계산, 상태 변경 버튼 클릭 리스너 등
        // 홈 화면의 실제 기능 코드를 작성합니다.
        // 예: Button btnStatusEdit = view.findViewById(R.id.btn_my_status_edit);
        //     btnStatusEdit.setOnClickListener(v -> { ... });

        return view;
    }
}
