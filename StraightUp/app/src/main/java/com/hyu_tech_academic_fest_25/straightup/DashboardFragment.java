package com.hyu_tech_academic_fest_25.straightup;

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
// [Phase 1 추가] ViewModelProvider import
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

    // [Phase 1 추가] ViewModel 선언
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

        // [Phase 1 추가] ViewModel 초기화
        // 'this' (Fragment)를 ViewModelStoreOwner로 전달
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // 뷰 초기화
        lineChart = view.findViewById(R.id.lineChart);
        tabLayout = view.findViewById(R.id.tabLayout);
        tvCvaValue = view.findViewById(R.id.tvCvaValue);
        tvStatusCircle = view.findViewById(R.id.tvStatusCircle);
        tvStatusLabel = view.findViewById(R.id.tvStatusLabel);


        setupChart();
        setupTabLayout();

        // [Phase 1 수정] ViewModel의 LiveData 구독
        observeViewModel();

        // (초기 데이터 로드는 ViewModel의 생성자에서 처리됨)
    }

    /**
     * [Phase 1 추가]
     * ViewModel의 LiveData를 관찰(observe)하여 UI를 업데이트합니다.
     */
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
                // [Phase 1 수정] 선택된 탭에 따라 ViewModel의 메서드 호출
                if (tab.getPosition() == 0) {
                    dashboardViewModel.loadChartDataForPeriod("today");
                } else if (tab.getPosition() == 1) {
                    dashboardViewModel.loadChartDataForPeriod("week");
                } else if (tab.getPosition() == 2) {
                    dashboardViewModel.loadChartDataForPeriod("month");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not used
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not used
            }
        });
    }

    // [Phase 1 삭제] loadChartDataForPeriod 메서드 (ViewModel으로 이동)
    // private void loadChartDataForPeriod(String period) { ... }

    /**
     * [Phase 1 수정]
     * CVA 값에 따라 UI를 업데이트하는 메서드
     * (로직은 ViewModel로 이동, 이 메서드는 순수하게 UI만 설정)
     * @param info ViewModel에서 계산된 UI 상태 정보
     */
    private void updateCvaDisplayUI(CvaDisplayInfo info) {
        tvCvaValue.setText(info.cvaText);
        tvCvaValue.setTextColor(info.cvaValueColor);

        tvStatusCircle.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), info.statusCircleBgColor));
        tvStatusCircle.setTextColor(info.statusTextColor);
        tvStatusCircle.setText(info.statusText);

        tvStatusLabel.setText(info.statusLabelText);
        tvStatusLabel.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }

    // [Phase 1 삭제] updateCvaDisplay(float cvaValue) 메서드 (로직이 ViewModel로 이동)
}