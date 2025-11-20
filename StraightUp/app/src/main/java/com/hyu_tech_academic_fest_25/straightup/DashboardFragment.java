package com.hyu_tech_academic_fest_25.straightup;

import android.content.res.ColorStateList; // [수정] import 추가
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    private LineChart lineChart;
    private TabLayout tabLayout;
    private TextView tvCvaValue;
    private TextView tvStatusCircle;
    private TextView tvStatusLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel 초기화
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // 뷰 초기화
        lineChart = view.findViewById(R.id.lineChart);
        tabLayout = view.findViewById(R.id.tabLayout);
        tvCvaValue = view.findViewById(R.id.tvCvaValue);
        tvStatusCircle = view.findViewById(R.id.tvStatusCircle);
        tvStatusLabel = view.findViewById(R.id.tvStatusLabel);

        setupChart();
        setupTabLayout();

        observeViewModel();
    }

    private void observeViewModel() {
        // 1. 차트 데이터 관찰
        dashboardViewModel.getChartData().observe(getViewLifecycleOwner(), lineData -> {
            if (lineData != null) {
                lineChart.setData(lineData);
                lineChart.invalidate();
            }
        });

        // 2. X축 레이블 관찰
        dashboardViewModel.getXAxisLabels().observe(getViewLifecycleOwner(), xLabels -> {
            if (xLabels != null) {
                lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value >= 0 && value < xLabels.length) {
                            return xLabels[(int) value];
                        }
                        return "";
                    }
                });
            }
        });

        // 3. CVA 상태 카드 UI 정보 관찰
        dashboardViewModel.getCvaDisplayInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                updateCvaDisplayUI(info);
            }
        });
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        lineChart.getAxisLeft().setAxisMinimum(40f);
        lineChart.getAxisLeft().setAxisMaximum(60f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    dashboardViewModel.loadChartDataForPeriod("today");
                } else if (tab.getPosition() == 1) {
                    dashboardViewModel.loadChartDataForPeriod("week");
                } else if (tab.getPosition() == 2) {
                    dashboardViewModel.loadChartDataForPeriod("month");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    /**
     * CVA 값에 따라 UI를 업데이트하는 메서드
     */
    private void updateCvaDisplayUI(CvaDisplayInfo info) {
        tvCvaValue.setText(info.cvaText);
        tvCvaValue.setTextColor(info.cvaValueColor); // 이건 원래 Color Int를 받으므로 정상

        // [수정 핵심] info.statusCircleBgColor는 이미 '색상값(Int)'입니다.
        // ContextCompat.getColorStateList는 '리소스 ID'를 원하므로 여기서 에러가 났습니다.
        // ColorStateList.valueOf()를 사용하여 색상값으로 바로 StateList를 만듭니다.
        tvStatusCircle.setBackgroundTintList(ColorStateList.valueOf(info.statusCircleBgColor));

        tvStatusCircle.setTextColor(info.statusTextColor);
        tvStatusCircle.setText(info.statusText);

        tvStatusLabel.setText(info.statusLabelText);
        tvStatusLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }
}