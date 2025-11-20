package com.hyu_tech_academic_fest_25.straightup;

import android.os.Bundle;
import android.util.Log; // Log import 추가

import androidx.annotation.NonNull; // NonNull import 추가
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// [Phase 1 추가] Firebase Auth import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // [Phase 1 추가] Log 태그
    private NavController navController;

    // [Phase 1 추가] Firebase Auth 인스턴스
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [Phase 1 추가] Firebase Auth 초기화 및 익명 로그인
        mAuth = FirebaseAuth.getInstance();
        signInAnonymously();


        // 1. 툴바 설정
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. NavHostFragment와 NavController 찾기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // 3. 툴바와 NavController 연결
            // (네비게이션 시 툴바의 타이틀이 자동으로 변경됩니다)
            NavigationUI.setupActionBarWithNavController(this, navController);

            // 4. BottomNavigationView와 NavController 연결
            // (바텀 네비 클릭 시 프래그먼트가 자동으로 전환됩니다)
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    /**
     * [Phase 1 추가]
     * Firebase 익명 인증을 수행합니다.
     * 보고서 10페이지의 Firebase 연동을 위한 사용자 UID 확보.
     */
    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "signInAnonymously:success. UID: " + user.getUid());
                            // Phase 2에서 이 UID를 사용하여 DB 경로를 설정합니다.
                        } else {
                            // 로그인 실패
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            // (실제 앱에서는 사용자에게 오류를 알리거나 재시도 로직 필요)
                        }
                    }
                });
    }


    // 툴바의 'Up' 버튼(뒤로가기)을 NavController와 연동
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}