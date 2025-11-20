package com.hyu_tech_academic_fest_25.straightup;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

/**
 * DashboardFragment의 UI 상태와 비즈니스 로직을 관리하는 ViewModel
 * AndroidViewModel을 상속하여 Application Context에 접근 (e.g., 리소스 접근)
 */
public class DashboardViewModel extends AndroidViewModel {

    // 1. LiveData 선언
    // 차트 데이터를 담을 LiveData
    private final MutableLiveData<LineData> chartData = new MutableLiveData<>();
    // CVA 상태 카드 UI 정보를 담을 LiveData
    private final MutableLiveData<CvaDisplayInfo> cvaDisplayInfo = new MutableLiveData<>();
    // X축 레이블을 담을 LiveData
    private final MutableLiveData<String[]> xAxisLabels = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        // ViewModel이 생성될 때 초기 데이터(샘플)를 로드합니다.
        // Phase 2에서는 이 부분이 Firebase 리스너로 대체됩니다.
        loadChartDataForPeriod("today");
        // 샘플 CVA 값으로 초기 상태 설정
        updateCvaDisplay(43.5f);
    }

    // 2. Fragment가 관찰할 LiveData Getter
    public LiveData<LineData> getChartData() {
        return chartData;
    }

    public LiveData<CvaDisplayInfo> getCvaDisplayInfo() {
        return cvaDisplayInfo;
    }

    public LiveData<String[]> getXAxisLabels() {
        return xAxisLabels;
    }

    // 3. 비즈니스 로직 (기존 Fragment에서 이동)
    // (Phase 2에서는 이 로직이 Firebase에서 데이터를 가져오도록 수정됩니다)
    public void loadChartDataForPeriod(String period) {
        ArrayList<Entry> emaEntries = new ArrayList<>();
        ArrayList<Entry> targetEntries = new ArrayList<>();
        String[] xLabels;

        switch (period) {
            case "today":
                emaEntries.add(new Entry(0, 50));
                emaEntries.add(new Entry(1, 48));
                emaEntries.add(new Entry(2, 49));
                emaEntries.add(new Entry(3, 45));
                emaEntries.add(new Entry(4, 46));
                emaEntries.add(new Entry(5, 43));
                emaEntries.add(new Entry(6, 43.5f));
                xLabels = new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};
                break;
            case "week":
                emaEntries.add(new Entry(0, 52));
                emaEntries.add(new Entry(1, 50));
                emaEntries.add(new Entry(2, 47));
                emaEntries.add(new Entry(3, 48));
                emaEntries.add(new Entry(4, 45));
                emaEntries.add(new Entry(5, 42));
                emaEntries.add(new Entry(6, 41.5f));
                xLabels = new String[]{"월", "화", "수", "목", "금", "토", "일"};
                break;
            case "month":
                emaEntries.add(new Entry(0, 55));
                emaEntries.add(new Entry(1, 53));
                emaEntries.add(new Entry(2, 51));
                emaEntries.add(new Entry(3, 49));
                emaEntries.add(new Entry(4, 47));
                emaEntries.add(new Entry(5, 45));
                emaEntries.add(new Entry(6, 44.2f));
                xLabels = new String[]{"1주차", "2주차", "3주차", "4주차", "5주차", "6주차", "7주차"};
                break;
            default:
                emaEntries.add(new Entry(0, 50));
                emaEntries.add(new Entry(1, 48));
                emaEntries.add(new Entry(2, 49));
                emaEntries.add(new Entry(3, 45));
                emaEntries.add(new Entry(4, 46));
                emaEntries.add(new Entry(5, 43));
                emaEntries.add(new Entry(6, 43.5f));
                xLabels = new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};
                break;
        }

        // X축 레이블 LiveData 업데이트
        this.xAxisLabels.setValue(xLabels);

        // --- LineDataSet 설정 (기존 로직) ---
        // (ViewModel은 Context에 직접 접근하면 안 되지만,
        // AndroidViewModel은 Application Context를 통해 리소스에 접근 가능합니다.)
        Application app = getApplication();
        LineDataSet emaDataSet = new LineDataSet(emaEntries, "CVA (EMA)");
        emaDataSet.setColor(ContextCompat.getColor(app, R.color.colorStatusRed));
        emaDataSet.setValueTextColor(Color.BLACK);
        emaDataSet.setCircleColor(ContextCompat.getColor(app, R.color.colorStatusRed));
        emaDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        emaDataSet.setDrawFilled(true);
        emaDataSet.setFillColor(ContextCompat.getColor(app, R.color.colorStatusRed));
        emaDataSet.setFillAlpha(30);

        // 목표 CVA 데이터
        for (int i = 0; i < emaEntries.size(); i++) {
            targetEntries.add(new Entry(i, 55));
        }

        LineDataSet targetDataSet = new LineDataSet(targetEntries, "목표 CVA");
        targetDataSet.setColor(Color.GREEN);
        targetDataSet.setDrawCircles(false);
        targetDataSet.enableDashedLine(10f, 5f, 0f);

        // LiveData에 최종 LineData 객체 설정 (postValue는 백그라운드, setValue는 메인 스레드)
        this.chartData.setValue(new LineData(emaDataSet, targetDataSet));
    }

    /**
     * CVA 값에 따라 UI 상태 정보를 계산하고 LiveData를 업데이트합니다.
     * (기존 Fragment의 로직 이동)
     * @param cvaValue 최신 CVA 값
     */
    public void updateCvaDisplay(float cvaValue) {
        Application app = getApplication();

        String cvaText = String.format("%.1f°", cvaValue);
        int statusCircleBgColor;
        int cvaValueColor;
        int statusTextColor;
        String statusText;
        String statusLabelText;

        // (보고서 7페이지 기준) CVA 값에 따른 분류 로직
        if (cvaValue < 45.0f) { // 중증도 (Severe)
            // colors.xml에 colorStatusRed가 정의되어 있어야 합니다.
            statusCircleBgColor = ContextCompat.getColor(app, R.color.colorStatusRed);
            statusTextColor = Color.WHITE;
            statusText = "위험";
            statusLabelText = "거북목 (Severe)";
        } else if (cvaValue >= 45.0f && cvaValue < 55.0f) { // 경미 (Mild)
            // TODO: colors.xml에 colorStatusOrange 추가 필요
            // (임시로 Android 기본 색상 사용)
            statusCircleBgColor = Color.parseColor("#F59E0B"); // (임시) Amber 500
            statusTextColor = Color.WHITE;
            statusText = "주의";
            statusLabelText = "거북목 (Mild)";
        } else { // 정상 (Normal)
            // TODO: colors.xml에 colorStatusGreen 추가 필요
            // (임시로 Android 기본 색상 사용)
            statusCircleBgColor = Color.parseColor("#10B981"); // (임시) Emerald 500
            statusTextColor = Color.WHITE;
            statusText = "정상";
            statusLabelText = "좋은 자세";
        }
        cvaValueColor = statusCircleBgColor; // CVA 값도 상태 색상에 맞게

        // CvaDisplayInfo 객체 생성
        CvaDisplayInfo info = new CvaDisplayInfo(
                cvaText, statusText, statusLabelText,
                cvaValueColor, statusCircleBgColor, statusTextColor
        );

        // LiveData 업데이트
        this.cvaDisplayInfo.setValue(info);
    }
}