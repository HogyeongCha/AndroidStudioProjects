package com.hyunji.ourlove; // 패키지 이름은 본인 환경에 맞게 수정하세요.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MemoriesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_memories.xml 레이아웃을 인플레이트(화면에 표시)합니다.
        View view = inflater.inflate(R.layout.fragment_memories, container, false);

        // TODO: 여기에 앨범, 지도, 맛집 리스트 버튼 클릭 리스너 등
        // '우리의 추억' 화면의 실제 기능 코드를 작성합니다.
        // 예: CardView cardAlbum = view.findViewById(R.id.card_album);
        //     cardAlbum.setOnClickListener(v -> { ... });

        return view;
    }
}
