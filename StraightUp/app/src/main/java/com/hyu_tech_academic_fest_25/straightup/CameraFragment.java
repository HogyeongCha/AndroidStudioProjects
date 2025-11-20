package com.hyu_tech_academic_fest_25.straightup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
// [Phase 1 추가] ViewModelProvider import
import androidx.lifecycle.ViewModelProvider;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    // [Phase 1 추가] ViewModel 선언
    private CameraViewModel cameraViewModel;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // 권한 요청 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 권한이 승인되면 카메라 시작
                    startCamera();
                } else {
                    // 권한이 거부되면 사용자에게 알림
                    Toast.makeText(getContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [Phase 1 추가] ViewModel 초기화
        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        previewView = view.findViewById(R.id.previewView);

        // 카메라 작업을 위한 백그라운드 스레드
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 권한 확인 및 카메라 시작
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        // [Phase 1 추가] ViewModel 관찰 (Phase 2에서 구현)
        // cameraViewModel.getTtsAlert().observe(getViewLifecycleOwner(), alertText -> {
        //     // TTS 또는 Toast 알림 로직
        // });
    }

    private boolean allPermissionsGranted() {
        // REQUIRED_PERMISSIONS 배열에 있는 모든 권한이 승인되었는지 확인
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        // 권한 요청 팝업 표시
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        // ProcessCameraProvider 인스턴스를 비동기적으로 가져옵니다.
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                // 카메라 공급자 가져오기
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 1. Preview UseCase 설정
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 2. CameraSelector 설정 (거북목 측정을 위해 전면 카메라 사용)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // (보고서 내용) 3. ImageAnalysis UseCase 설정 (Phase 2에서 구현)
                // ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                //         .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                //         .build();
                // imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                //     // [Phase 2] ViewModel으로 이미지 전달
                //     cameraViewModel.processImage(image);
                //     image.close();
                // });

                // 기존 바인딩 해제
                cameraProvider.unbindAll();

                // UseCase를 라이프사이클에 바인딩
                // (Phase 2에서 imageAnalysis를 콤마로 추가)
                cameraProvider.bindToLifecycle(
                        this, // Fragment의 ViewLifecycleOwner
                        cameraSelector,
                        preview
                        // , imageAnalysis
                );

            } catch (Exception e) {
                Log.e(TAG, "CameraX 바인딩 실패", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 프래그먼트가 파괴될 때 스레드 풀 종료
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}