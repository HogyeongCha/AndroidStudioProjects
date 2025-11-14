package com.hyu_tech_academic_fest_25.straightup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 데이터 보존 기간 Spinner 설정
        Spinner retentionSpinner = view.findViewById(R.id.spinnerDataRetention);

        // strings.xml에 정의된 string-array를 사용하여 어댑터 생성
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.data_retention_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 스피너에 어댑터 적용
        retentionSpinner.setAdapter(adapter);

        // 기본값 '1개월'로 설정 (배열의 1번 인덱스)
        retentionSpinner.setSelection(1);
    }
}