package com.hyu_tech_academic_fest_25.straightup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    // 툴바의 'Up' 버튼(뒤로가기)을 NavController와 연동
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}