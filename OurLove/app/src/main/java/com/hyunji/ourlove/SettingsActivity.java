package com.hyunji.ourlove;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentStartDate;
    private Button btnChangeStartDate;
    private Switch switchNotifications;
    private Button btnLogout;
    private Button btnDisconnectCouple;

    private Calendar selectedCalendar;

    private static final String PREF_NAME = "OurLovePrefs";
    private static final String KEY_START_DATE = "couple_start_date";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvCurrentStartDate = findViewById(R.id.tv_current_start_date);
        btnChangeStartDate = findViewById(R.id.btn_change_start_date);
        switchNotifications = findViewById(R.id.switch_notifications);
        btnLogout = findViewById(R.id.btn_logout);
        btnDisconnectCouple = findViewById(R.id.btn_disconnect_couple);

        selectedCalendar = Calendar.getInstance();

        // 만난 날짜 로드 및 표시
        loadAndDisplayStartDate();

        // 알림 설정 로드 및 적용
        loadNotificationSettings();

        btnChangeStartDate.setOnClickListener(v -> showDatePickerDialog());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> saveNotificationSettings(isChecked));
        btnLogout.setOnClickListener(v -> logoutUser());
        btnDisconnectCouple.setOnClickListener(v -> showDisconnectConfirmationDialog());
    }

    private void loadAndDisplayStartDate() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedDateStr = prefs.getString(KEY_START_DATE, null);

        if (savedDateStr != null) {
            try {
                // YYYY-MM-DD 형식으로 저장된 날짜를 Calendar 객체로 파싱
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                selectedCalendar.setTime(sdf.parse(savedDateStr));
                updateStartDateTextView();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
                tvCurrentStartDate.setText("만난 날짜를 설정해주세요.");
            }
        } else {
            tvCurrentStartDate.setText("만난 날짜를 설정해주세요.");
        }
    }

    private void updateStartDateTextView() {
        String myFormat = "yyyy년 MM월 dd일";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        tvCurrentStartDate.setText("현재 날짜: " + sdf.format(selectedCalendar.getTime()));
    }

    private void showDatePickerDialog() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedCalendar.set(year1, monthOfYear, dayOfMonth);
                    updateStartDateTextView();
                    saveStartDate(selectedCalendar);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void saveStartDate(Calendar calendar) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        editor.putString(KEY_START_DATE, sdf.format(calendar.getTime()));
        editor.apply();
        Toast.makeText(this, "만난 날짜가 변경되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void loadNotificationSettings() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // 기본값 true
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void saveNotificationSettings(boolean isEnabled) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isEnabled);
        editor.apply();
        Toast.makeText(this, "알림 설정이 " + (isEnabled ? "켜졌습니다." : "꺼졌습니다."), Toast.LENGTH_SHORT).show();
    }

    private void showDisconnectConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("커플 연결 끊기")
                .setMessage("정말로 커플 연결을 끊으시겠습니까? 이 작업은 되돌릴 수 없습니다.")
                .setPositiveButton("예", (dialog, which) -> disconnectCouple())
                .setNegativeButton("아니오", null)
                .show();
    }

    private void disconnectCouple() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        String currentUserId = currentUser.getUid();

        mDatabase.child("users").child(currentUserId).child("coupleId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String coupleId = snapshot.getValue(String.class);
                if (coupleId != null) {
                    mDatabase.child("users").orderByChild("coupleId").equalTo(coupleId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot partnerSnapshot) {
                                    for (DataSnapshot userSnap : partnerSnapshot.getChildren()) {
                                        String userId = userSnap.getKey();
                                        if (userId != null) {
                                            mDatabase.child("users").child(userId).child("coupleId").removeValue();
                                            mDatabase.child("users").child(userId).child("connectCode").removeValue();
                                        }
                                    }
                                    mDatabase.child("couples").child(coupleId).removeValue();

                                    SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove(KEY_START_DATE);
                                    editor.apply();

                                    Toast.makeText(SettingsActivity.this, "커플 연결이 끊어졌습니다.", Toast.LENGTH_LONG).show();
                                    navigateToLogin();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(SettingsActivity.this, "연결 끊기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(SettingsActivity.this, "연결된 커플이 없습니다.", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        navigateToLogin();
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}