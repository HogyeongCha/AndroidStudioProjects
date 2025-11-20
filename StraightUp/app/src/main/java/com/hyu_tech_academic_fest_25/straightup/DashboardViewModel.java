package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DashboardViewModel extends AndroidViewModel {

    private static final String TAG = "DashboardViewModel";

    private final MutableLiveData<LineData> chartData = new MutableLiveData<>();
    private final MutableLiveData<CvaDisplayInfo> cvaDisplayInfo = new MutableLiveData<>();
    private final MutableLiveData<String[]> xAxisLabels = new MutableLiveData<>();

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        startListeningToFirebaseData();
    }

    public LiveData<LineData> getChartData() { return chartData; }
    public LiveData<CvaDisplayInfo> getCvaDisplayInfo() { return cvaDisplayInfo; }
    public LiveData<String[]> getXAxisLabels() { return xAxisLabels; }

    public void loadChartDataForPeriod(String period) {
        startListeningToFirebaseData();
    }

    private void startListeningToFirebaseData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.w(TAG, "User is null. Cannot load data yet.");
            updateCvaDisplay(0);
            return;
        }

        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }

        try {
            databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("cvaHistory");

            valueEventListener = databaseReference.limitToLast(50).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        processDataSnapshot(snapshot);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing data snapshot", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to read value.", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firebase listener", e);
        }
    }

    private void processDataSnapshot(DataSnapshot snapshot) {
        List<CvaDataPoint> dataList = new ArrayList<>();
        if (snapshot.exists()) {
            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                try {
                    CvaDataPoint dataPoint = postSnapshot.getValue(CvaDataPoint.class);
                    // [Task 3] 데이터 유효성 필터링 강화 (10도~180도 사이의 유효한 값만 사용)
                    if (dataPoint != null && dataPoint.getCva() > 10.0 && dataPoint.getCva() < 180.0) {
                        dataList.add(dataPoint);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing data point", e);
                }
            }
        }

        if (dataList.isEmpty()) {
            Log.i(TAG, "No data found. Generating mock data for demo...");
            generateMockData();
            return;
        }

        List<Entry> emaEntries = calculateEMA(dataList);
        String[] xLabels = generateXAxisLabels(dataList);
        xAxisLabels.setValue(xLabels);
        updateChartData(emaEntries);

        // [Task 2 핵심] EMA 값을 기준으로 현재 상태 및 변화량 계산
        if (!emaEntries.isEmpty()) {
            // 1. 최신 EMA 값 (현재 내 점수)
            float currentCva = emaEntries.get(emaEntries.size() - 1).getY();

            // 2. 변화량 계산 (직전 EMA 값과 비교)
            float diff = 0.0f;
            if (emaEntries.size() >= 2) {
                float prevCva = emaEntries.get(emaEntries.size() - 2).getY();
                diff = currentCva - prevCva;
            }

            // 3. 통합 메서드 호출 (모든 화면에 일관된 데이터 전달)
            updateCvaDisplay(currentCva, diff);
        } else {
            updateCvaDisplay(0); // EMA 계산 실패 시 초기화
        }
    }

    private void generateMockData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("cvaHistory");

        Random random = new Random();
        long currentTime = System.currentTimeMillis();

        for (int i = 29; i >= 0; i--) {
            long timestamp = currentTime - (i * 60 * 1000);
            double cva = 42.0 + (random.nextDouble() * 16.0);
            String classification;
            if (cva >= 53.0) classification = "Normal";
            else if (cva >= 45.0) classification = "Mild";
            else classification = "Severe";

            CvaDataPoint mockData = new CvaDataPoint(timestamp, cva, classification);
            ref.push().setValue(mockData);
        }
    }

    private List<Entry> calculateEMA(List<CvaDataPoint> dataList) {
        List<Entry> entries = new ArrayList<>();
        if (dataList.isEmpty()) return entries;

        double alpha = 0.2;
        double ema = dataList.get(0).getCva();

        // [Task 3] EMA 계산 검증 로그 추가
        Log.d(TAG, "=== EMA 계산 시작 (Alpha: " + alpha + ") ===");

        for (int i = 0; i < dataList.size(); i++) {
            double rawValue = dataList.get(i).getCva();
            // EMA 공식 적용
            ema = (alpha * rawValue) + ((1 - alpha) * ema);
            entries.add(new Entry(i, (float) ema));

            // [Task 3] Raw 값과 EMA 값 비교 출력
            Log.d(TAG, String.format(Locale.getDefault(),
                    "Data[%d]: Raw=%.2f -> EMA=%.2f", i, rawValue, ema));
        }
        Log.d(TAG, "=== EMA 계산 종료 ===");

        return entries;
    }

    private String[] generateXAxisLabels(List<CvaDataPoint> dataList) {
        String[] labels = new String[dataList.size()];
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (int i = 0; i < dataList.size(); i++) {
            labels[i] = sdf.format(new Date(dataList.get(i).getTimestamp()));
        }
        return labels;
    }

    private void updateChartData(List<Entry> emaEntries) {
        Application app = getApplication();
        LineDataSet emaDataSet = new LineDataSet(emaEntries, "CVA (EMA)");
        int redColor = ContextCompat.getColor(app, R.color.colorStatusRed);

        emaDataSet.setColor(redColor);
        emaDataSet.setValueTextColor(Color.BLACK);

        // [Task 3] EMA 선이 연속적으로 보이도록 데이터 포인트를 제거합니다.
        emaDataSet.setDrawCircles(false);

        emaDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        emaDataSet.setDrawFilled(true);
        emaDataSet.setFillColor(redColor);
        emaDataSet.setFillAlpha(30);
        emaDataSet.setDrawValues(false);

        List<Entry> targetEntries = new ArrayList<>();
        for (int i = 0; i < emaEntries.size(); i++) {
            targetEntries.add(new Entry(i, 55));
        }
        LineDataSet targetDataSet = new LineDataSet(targetEntries, "목표 CVA");
        targetDataSet.setColor(Color.GREEN);
        targetDataSet.setDrawCircles(false);
        targetDataSet.enableDashedLine(10f, 5f, 0f);
        targetDataSet.setDrawValues(false);

        chartData.setValue(new LineData(emaDataSet, targetDataSet));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    // [Task 2 핵심] 새로운 메인 로직: diff 인수를 포함 (7개 인수 생성자 호출)
    public void updateCvaDisplay(float cvaValue, float diff) {
        Application app = getApplication();

        if (cvaValue == 0) {
            // 초기값/데이터 없음 시 diff도 0으로 설정
            // CvaDisplayInfo는 7개 인수를 받습니다.
            CvaDisplayInfo info = new CvaDisplayInfo(
                    "-", "측정 대기", "데이터 없음",
                    Color.GRAY, Color.LTGRAY, Color.DKGRAY, 0.0
            );
            cvaDisplayInfo.setValue(info);
            return;
        }

        String cvaText = String.format("%.1f°", cvaValue);
        int statusCircleBgColor;
        int cvaValueColor;
        int statusTextColor;
        String statusText;
        String statusLabelText;

        if (cvaValue < 45.0f) {
            statusCircleBgColor = ContextCompat.getColor(app, R.color.colorStatusRed);
            statusTextColor = Color.WHITE;
            statusText = "위험";
            statusLabelText = "거북목 (Severe)";
        } else if (cvaValue >= 45.0f && cvaValue < 55.0f) {
            statusCircleBgColor = Color.parseColor("#F59E0B");
            statusTextColor = Color.WHITE;
            statusText = "주의";
            statusLabelText = "거북목 (Mild)";
        } else {
            statusCircleBgColor = Color.parseColor("#10B981");
            statusTextColor = Color.WHITE;
            statusText = "정상";
            statusLabelText = "좋은 자세";
        }
        cvaValueColor = statusCircleBgColor;

        // [핵심] diff를 포함하여 CvaDisplayInfo 생성 (7 arguments)
        CvaDisplayInfo info = new CvaDisplayInfo(
                cvaText, statusText, statusLabelText,
                cvaValueColor, statusCircleBgColor, statusTextColor, (double) diff
        );
        cvaDisplayInfo.setValue(info);
    }

    // [Task 2] 오버로딩 추가: 단일 인수로 호출하는 기존 코드를 위한 호환성 유지
    /**
     * 기존 코드에서 updateCvaDisplay(cvaValue) 형태로 호출되는 경우를 처리합니다.
     */
    public void updateCvaDisplay(float cvaValue) {
        // 변화량을 0.0f로 설정하여 새 함수를 호출합니다.
        updateCvaDisplay(cvaValue, 0.0f);
    }
}