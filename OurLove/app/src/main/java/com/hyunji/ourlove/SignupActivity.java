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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginPrompt;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);

        btnSignup.setOnClickListener(v -> registerUser());
        tvLoginPrompt.setOnClickListener(v -> {
            finish(); // 로그인 페이지로 돌아감
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해주세요.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("비밀번호를 입력해주세요.");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("비밀번호가 일치하지 않습니다.");
            etConfirmPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 사용자 정보 (예: UID)를 Realtime Database에 저장할 수 있습니다.
                                // 이 예제에서는 사용자 ID만 저장합니다.
                                mDatabase.child("users").child(user.getUid()).child("email").setValue(user.getEmail());
                                Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                // 회원가입 성공 시 CoupleConnectActivity로 이동
                                startActivity(new Intent(SignupActivity.this, CoupleConnectActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}