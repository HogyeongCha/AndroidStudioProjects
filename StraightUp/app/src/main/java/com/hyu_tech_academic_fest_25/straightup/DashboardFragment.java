package com.hyu_tech_academic_fest_25.straightup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML 레이아웃 인플레이트
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.lineChart);

        setupChart();
        loadChartData();
    }

    private void setupChart() {
        // 차트 기본 설정
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // X축 설정
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        // X축 레이블 포매터 (예: "09:00", "10:00")
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] labels = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};
            @Override
            public String getFormattedValue(float value) {
                return labels[(int) value % labels.length];
            }
        });

        // Y축 (왼쪽)
        lineChart.getAxisLeft().setAxisMinimum(40f);
        lineChart.getAxisLeft().setAxisMaximum(60f);

        // Y축 (오른쪽)
        lineChart.getAxisRight().setEnabled(false);

        // 범례
        lineChart.getLegend().setEnabled(true);
    }

    private void loadChartData() {
        // 1. CVA (EMA) 데이터 (HTML 시안과 동일한 샘플 데이터)
        ArrayList<Entry> emaEntries = new ArrayList<>();
        emaEntries.add(new Entry(0, 50));
        emaEntries.add(new Entry(1, 48));
        emaEntries.add(new Entry(2, 49));
        emaEntries.add(new Entry(3, 45));
        emaEntries.add(new Entry(4, 46));
        emaEntries.add(new Entry(5, 43));
        emaEntries.add(new Entry(6, 43.5f));

        LineDataSet emaDataSet = new LineDataSet(emaEntries, "CVA (EMA)");
        emaDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorStatusRed)); // colors.xml의 red_500
        emaDataSet.setValueTextColor(Color.BLACK);
        emaDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorStatusRed));
        emaDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 부드러운 곡선
        emaDataSet.setDrawFilled(true);
        emaDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.colorStatusRed));
        emaDataSet.setFillAlpha(30);

        // 2. 목표 CVA 데이터
        ArrayList<Entry> targetEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            targetEntries.add(new Entry(i, 55));
        }

        LineDataSet targetDataSet = new LineDataSet(targetEntries, "목표 CVA");
        targetDataSet.setColor(Color.GREEN);
        targetDataSet.setDrawCircles(false);
        targetDataSet.enableDashedLine(10f, 5f, 0f); // 점선

        // 차트에 데이터 적용
        LineData lineData = new LineData(emaDataSet, targetDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // 차트 새로고침
    }
}