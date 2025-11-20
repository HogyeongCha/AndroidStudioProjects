package com.hyu_tech_academic_fest_25.straightup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    // 권한 요청용 (현재 코드에서는 직접 배열을 사용하지 않으나 유지)
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private CameraViewModel cameraViewModel;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private PoseAnalyzer poseAnalyzer;
    // 이미지 분석 중복 실행 방지를 위한 플래그
    private AtomicBoolean isAnalyzing = new AtomicBoolean(false);

    // 권한 요청 결과 처리 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
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

        // ViewModel 초기화 (Activity 범위 공유 가능하지만 여기서는 Fragment 범위로 사용)
        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        // 분석기 초기화
        poseAnalyzer = new PoseAnalyzer(requireContext());

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 권한 확인 및 카메라 시작
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        // ViewModel의 알림 LiveData 관찰 (TTS/Toast 알림 트리거)
        cameraViewModel.getTtsAlert().observe(getViewLifecycleOwner(), alertText -> {
            if (alertText != null && !alertText.isEmpty()) {
                // (나중에 TTS 기능 추가 시 여기서 처리)
                Toast.makeText(getContext(), alertText, Toast.LENGTH_SHORT).show();
                // 알림을 한 번만 띄우기 위해 LiveData 초기화
                cameraViewModel.clearTtsAlert();
            }
        });
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 전면 카메라 사용 (거북목 측정용)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // 이미지 분석 UseCase 설정
                // OUTPUT_IMAGE_FORMAT_RGBA_8888을 사용하여 YUV 변환 과정을 생략하고 바로 Bitmap 변환 가능
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    // 이전 분석이 아직 진행 중이라면 현재 프레임은 드롭 (중복 실행 방지)
                    if (isAnalyzing.compareAndSet(false, true)) {
                        analyzeImage(image);
                    } else {
                        image.close();
                    }
                });

                // 기존 바인딩 해제 후 다시 바인딩
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                Log.e(TAG, "CameraX 바인딩 실패", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * ImageProxy를 Bitmap으로 변환하고 분석을 수행합니다.
     */
    private void analyzeImage(ImageProxy image) {
        // 1. RGBA 버퍼에서 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        bitmap.copyPixelsFromBuffer(buffer);

        // 2. 전면 카메라 회전 및 좌우 반전 보정
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        // 전면 카메라는 거울 모드(좌우 반전) 적용
        matrix.postScale(-1f, 1f, image.getWidth() / 2f, image.getHeight() / 2f);

        // 최종 분석용 비트맵 생성
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, image.getWidth(), image.getHeight(), matrix, true
        );

        // 3. PoseAnalyzer로 분석 수행
        AnalysisResult result = poseAnalyzer.analyze(rotatedBitmap);

        // 4. ViewModel으로 결과 전달 (UI 스레드에서 안전하게 실행)
        // isAdded() 체크를 통해 프래그먼트가 화면에 붙어있을 때만 UI 업데이트 시도
        if (result != null && isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    // [수정] Nano-Banana 시뮬레이션을 위해 rotatedBitmap 원본도 함께 전달
                    // 주의: 여기서 bitmap을 recycle()하면 안됨 (ViewModel에서 참조할 수 있음)
                    cameraViewModel.processImageAnalysisResult(result, rotatedBitmap);
                }
            });
        }

        // 5. 분석 완료 플래그 해제 및 이미지 닫기
        isAnalyzing.set(false);
        image.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 프래그먼트 파괴 시 실행자 종료
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}