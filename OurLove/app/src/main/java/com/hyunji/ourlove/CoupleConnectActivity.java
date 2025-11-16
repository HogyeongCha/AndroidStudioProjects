package com.hyunji.ourlove;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate; // 추가
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CoupleConnectActivity extends AppCompatActivity {

    private TextView tvGeneratedCode;
    private EditText etPartnerCode;
    private Button btnGenerateCode, btnConnectCouple;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple_connect);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // 로그인되지 않은 사용자면 로그인 페이지로 이동
            startActivity(new Intent(CoupleConnectActivity.this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        tvGeneratedCode = findViewById(R.id.tv_generated_code);
        etPartnerCode = findViewById(R.id.et_partner_code);
        btnGenerateCode = findViewById(R.id.btn_generate_code);
        btnConnectCouple = findViewById(R.id.btn_connect_couple);

        // 이미 커플로 연결되어 있는지 확인
        checkIfAlreadyConnected();

        btnGenerateCode.setOnClickListener(v -> generateUniqueCode());
        btnConnectCouple.setOnClickListener(v -> connectCouple());
    }

    private void checkIfAlreadyConnected() {
        mDatabase.child("users").child(currentUserId).child("coupleId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(String.class) != null) {
                    // 이미 coupleId가 있다면 MainActivity로 이동
                    Toast.makeText(CoupleConnectActivity.this, "이미 커플로 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CoupleConnectActivity.this, MainActivity.class));
                    finish();
                } else {
                    // 연결 코드가 있는지 확인 (이전에 생성했지만 연결 안 된 경우)
                    mDatabase.child("users").child(currentUserId).child("connectCode").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue(String.class) != null) {
                                tvGeneratedCode.setText("내 코드: " + snapshot.getValue(String.class));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // 에러 처리
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CoupleConnectActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateUniqueCode() {
        String uniqueCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT); // 6자리 코드 생성

        // 생성된 코드를 사용자 정보에 저장
        mDatabase.child("users").child(currentUserId).child("connectCode").setValue(uniqueCode)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvGeneratedCode.setText("내 코드: " + uniqueCode);
                        Toast.makeText(CoupleConnectActivity.this, "연결 코드가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CoupleConnectActivity.this, "코드 생성 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void connectCouple() {
        String partnerCode = etPartnerCode.getText().toString().trim().toUpperCase(Locale.ROOT);

        if (TextUtils.isEmpty(partnerCode)) {
            etPartnerCode.setError("상대방의 코드를 입력해주세요.");
            etPartnerCode.requestFocus();
            return;
        }

        // 상대방의 코드를 가진 사용자를 찾습니다.
        mDatabase.child("users").orderByChild("connectCode").equalTo(partnerCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String partnerId = userSnapshot.getKey();
                                if (partnerId != null && !partnerId.equals(currentUserId)) {
                                    // 상대방을 찾았고, 본인이 아닌 경우
                                    createCouple(partnerId);
                                    return;
                                }
                            }
                            Toast.makeText(CoupleConnectActivity.this, "유효하지 않은 코드이거나 본인 코드입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoupleConnectActivity.this, "일치하는 코드를 가진 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CoupleConnectActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createCouple(String partnerId) {
        String coupleId = mDatabase.child("couples").push().getKey(); // 새로운 coupleId 생성

        if (coupleId != null) {
            // 각 사용자의 coupleId 업데이트
            Map<String, Object> updates = new HashMap<>();
            updates.put("users/" + currentUserId + "/coupleId", coupleId);
            updates.put("users/" + partnerId + "/coupleId", coupleId);

            // 커플 정보 생성 (예: 시작일, 이름 등)
            Map<String, Object> coupleInfo = new HashMap<>();
            // coupleInfo.put("startDate", "" + LocalDate.now()); // 이제 SetStartDateActivity에서 설정하므로 주석 처리
            // TODO: 나중에 사용자 이름 등을 추가할 수 있음
            updates.put("couples/" + coupleId, coupleInfo);

            mDatabase.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CoupleConnectActivity.this, "커플 연결 성공!", Toast.LENGTH_SHORT).show();
                            // SetStartDateActivity로 이동
                            startActivity(new Intent(CoupleConnectActivity.this, SetStartDateActivity.class));
                            finish();
                        } else {
                            Toast.makeText(CoupleConnectActivity.this, "커플 연결 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}