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

public class MemoriesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memories, container, false);

        Button btnAlbumGo = view.findViewById(R.id.btn_album_go);
        if (btnAlbumGo != null) {
            btnAlbumGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "앨범 보러가기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnMapGo = view.findViewById(R.id.btn_map_go);
        if (btnMapGo != null) {
            btnMapGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "지도 펼치기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnFoodGo = view.findViewById(R.id.btn_food_go);
        if (btnFoodGo != null) {
            btnFoodGo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "맛집 리스트 보기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }
}