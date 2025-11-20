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
import com.github.mikephil.charting.formatter.ValueFormatter;
// Firebase import
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

public class DashboardViewModel extends AndroidViewModel {

    private static final String TAG = "DashboardViewModel";

    private final MutableLiveData<LineData> chartData = new MutableLiveData<>();
    private final MutableLiveData<CvaDisplayInfo> cvaDisplayInfo = new MutableLiveData<>();
    private final MutableLiveData<String[]> xAxisLabels = new MutableLiveData<>();

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        // [수정] 생성자에서는 리스너를 바로 붙이지 않고, 초기화만 합니다.
        // 데이터 로드는 Fragment에서 명시적으로 호출하거나, 여기서 호출하되 안전하게 처리합니다.
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

        // [수정] User가 null이면 로그를 남기고 리턴 (앱 죽음 방지)
        if (user == null) {
            Log.w(TAG, "User is null. Cannot load data yet.");
            // 빈 데이터라도 보여주기 위해 초기화
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
                    if (dataPoint != null && dataPoint.getCva() > 0.0) {
                        dataList.add(dataPoint);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing data point", e);
                }
            }
        }

        if (dataList.isEmpty()) {
            updateCvaDisplay(0);
            // 빈 차트 데이터 설정 (오류 방지)
            chartData.setValue(new LineData());
            return;
        }

        List<Entry> emaEntries = calculateEMA(dataList);
        String[] xLabels = generateXAxisLabels(dataList);
        xAxisLabels.setValue(xLabels);
        updateChartData(emaEntries);

        CvaDataPoint lastData = dataList.get(dataList.size() - 1);
        updateCvaDisplay((float) lastData.getCva());
    }

    // ... (calculateEMA, generateXAxisLabels, updateChartData 메서드는 기존과 동일) ...
    // (코드가 길어져서 중략, 기존 메서드 그대로 유지해주세요)
    private List<Entry> calculateEMA(List<CvaDataPoint> dataList) {
        List<Entry> entries = new ArrayList<>();
        if (dataList.isEmpty()) return entries;
        double alpha = 0.2;
        double ema = dataList.get(0).getCva();
        for (int i = 0; i < dataList.size(); i++) {
            double currentValue = dataList.get(i).getCva();
            ema = (alpha * currentValue) + ((1 - alpha) * ema);
            entries.add(new Entry(i, (float) ema));
        }
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
        // 색상 리소스 접근 시 예외 발생 가능성 차단
        int redColor = ContextCompat.getColor(app, R.color.colorStatusRed);

        emaDataSet.setColor(redColor);
        emaDataSet.setValueTextColor(Color.BLACK);
        emaDataSet.setCircleColor(redColor);
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

    public void updateCvaDisplay(float cvaValue) {
        Application app = getApplication();

        if (cvaValue == 0) {
            CvaDisplayInfo info = new CvaDisplayInfo(
                    "-", "측정 대기", "데이터 없음",
                    Color.GRAY, Color.LTGRAY, Color.DKGRAY
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

        CvaDisplayInfo info = new CvaDisplayInfo(
                cvaText, statusText, statusLabelText,
                cvaValueColor, statusCircleBgColor, statusTextColor
        );
        cvaDisplayInfo.setValue(info);
    }
}