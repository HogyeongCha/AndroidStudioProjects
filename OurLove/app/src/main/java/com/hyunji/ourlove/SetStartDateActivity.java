package com.hyunji.ourlove;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SetStartDateActivity extends AppCompatActivity {

    private TextView tvSelectedDate;
    private Button btnSelectDate, btnComplete;
    private Calendar selectedCalendar;

    private static final String PREF_NAME = "OurLovePrefs";
    private static final String KEY_START_DATE = "couple_start_date"; // YYYY-MM-DD 형식

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_start_date);

        tvSelectedDate = findViewById(R.id.tv_selected_date);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnComplete = findViewById(R.id.btn_complete);

        selectedCalendar = Calendar.getInstance();

        // 이전에 저장된 날짜가 있다면 불러와서 표시
        loadStartDate();

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnComplete.setOnClickListener(v -> saveAndNavigateToMain());
    }

    private void showDatePickerDialog() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedCalendar.set(year1, monthOfYear, dayOfMonth);
                    updateDateTextView();
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateDateTextView() {
        String myFormat = "yyyy년 MM월 dd일"; // 날짜 포맷
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        tvSelectedDate.setText(sdf.format(selectedCalendar.getTime()));
    }

    private void loadStartDate() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedDateStr = prefs.getString(KEY_START_DATE, null);

        if (savedDateStr != null) {
            try {
                // YYYY-MM-DD 형식으로 저장된 날짜를 Calendar 객체로 파싱
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                selectedCalendar.setTime(sdf.parse(savedDateStr));
                updateDateTextView();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
                // 파싱 오류 시 기본값 설정
                tvSelectedDate.setText("날짜를 선택해주세요.");
            }
        }
    }

    private void saveAndNavigateToMain() {
        // 선택된 날짜가 있는지 확인
        if (tvSelectedDate.getText().toString().equals("날짜를 선택해주세요.")) {
            Toast.makeText(this, "만난 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 날짜를 YYYY-MM-DD 형식의 문자열로 저장
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        editor.putString(KEY_START_DATE, sdf.format(selectedCalendar.getTime()));
        editor.apply();

        Toast.makeText(this, "만난 날짜가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        // 메인 액티비티로 이동
        startActivity(new Intent(SetStartDateActivity.this, MainActivity.class));
        finish();
    }
}