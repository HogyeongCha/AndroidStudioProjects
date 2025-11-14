package com.hyunji.ourlove; // 패키지 이름은 본인 환경에 맞게 수정하세요.

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class    MainActivity extends AppCompatActivity {

    // 4개의 프래그먼트 객체를 미리 생성해둡니다.
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment playFragment = new PlayFragment();
    private final Fragment memoriesFragment = new MemoriesFragment();
    private final Fragment analysisFragment = new AnalysisFragment();

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_main.xml 레이아웃을 화면에 표시합니다.
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        // 하단 네비게이션 뷰를 찾습니다.
        BottomNavigationView navView = findViewById(R.id.bottom_navigation_view);

        // 앱이 처음 시작될 때 홈 프래그먼트(HomeFragment)를 기본으로 보여줍니다.
        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.frame_layout, homeFragment).commit();
        }

        // 하단 네비게이션 뷰의 아이템(메뉴) 클릭 리스너를 설정합니다.
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // '홈' 탭 클릭 시
                replaceFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_play) {
                // '같이 놀기' 탭 클릭 시
                replaceFragment(playFragment);
                return true;
            } else if (itemId == R.id.navigation_memories) {
                // '우리의 추억' 탭 클릭 시
                replaceFragment(memoriesFragment);
                return true;
            } else if (itemId == R.id.navigation_analysis) {
                // 'AI 분석실' 탭 클릭 시
                replaceFragment(analysisFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * FrameLayout의 프래그먼트를 교체하는 메서드입니다.
     * @param fragment 교체할 프래그먼트 객체
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        // transaction.addToBackStack(null); // 뒤로가기 스택에 추가 (선택 사항)
        transaction.commit();
    }
}
