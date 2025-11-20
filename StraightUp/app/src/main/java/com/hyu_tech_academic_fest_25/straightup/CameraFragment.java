package com.hyu_tech_academic_fest_25.straightup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color; // [추가] Color 클래스 import
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // [추가] TextView import
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

import android.speech.tts.TextToSpeech;
import java.util.Locale;


public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private CameraViewModel cameraViewModel;

    private PreviewView previewView;
    private TextView tvResultLabel; // [추가] 결과 라벨 TextView 변수 선언

    private ExecutorService cameraExecutor;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private PoseAnalyzer poseAnalyzer;
    private AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    private TextToSpeech tts; // TTS 객체 선언

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

        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);
        poseAnalyzer = new PoseAnalyzer(requireContext());

        previewView = view.findViewById(R.id.previewView);
        // [추가] TextView 연결
        tvResultLabel = view.findViewById(R.id.tvResultLabel);

        cameraExecutor = Executors.newSingleThreadExecutor();
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.KOREAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "한국어를 지원하지 않는 기기입니다.");
                }
            }
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        // [수정] 실시간 분석 결과 관찰
        cameraViewModel.getLatestCva().observe(getViewLifecycleOwner(), dataPoint -> {
            if (dataPoint != null) {
                // [수정] CVA 값도 함께 전달
                updateStatusUI(dataPoint.getCva(), dataPoint.getClassification());
            }
        });

        cameraViewModel.getTtsAlert().observe(getViewLifecycleOwner(), alertText -> {
            if (alertText != null && !alertText.isEmpty()) {
                // 1. 화면에 토스트도 띄우고
                Toast.makeText(getContext(), alertText, Toast.LENGTH_SHORT).show();

                // 2. [핵심] 실제 음성으로 출력 (부스 시연용)
                if (tts != null) {
                    tts.speak(alertText, TextToSpeech.QUEUE_FLUSH, null, null);
                }

                cameraViewModel.clearTtsAlert();
            }
        });
    }

    /**
     * [수정] 상태와 CVA 값을 함께 받아 UI 업데이트
     */
    private void updateStatusUI(double cva, String classification) {
        if (tvResultLabel == null) return;

        // [핵심] 텍스트 포맷 변경: "Severe (35.2°)" 형태로 표시
        String statusText = String.format(Locale.getDefault(), "%s (%.1f°)", classification, cva);
        tvResultLabel.setText(statusText);

        int color;
        switch (classification) {
            case "Normal":
                color = Color.parseColor("#10B981"); // 초록
                break;
            case "Mild":
                color = Color.parseColor("#F59E0B"); // 주황
                break;
            case "Severe":
                color = Color.parseColor("#EF4444"); // 빨강
                break;
            default:
                color = Color.GRAY;
                break;
        }
        tvResultLabel.setBackgroundColor(color);
    }

    
    // ... (이하 startCamera, analyzeImage 등의 메서드는 기존과 동일하게 유지) ...

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

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if (isAnalyzing.compareAndSet(false, true)) {
                        analyzeImage(image);
                    } else {
                        image.close();
                    }
                });

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

    private void analyzeImage(ImageProxy image) {
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        bitmap.copyPixelsFromBuffer(buffer);

        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        matrix.postScale(-1f, 1f, image.getWidth() / 2f, image.getHeight() / 2f);

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, image.getWidth(), image.getHeight(), matrix, true
        );

        AnalysisResult result = poseAnalyzer.analyze(rotatedBitmap);

        if (result != null && isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    cameraViewModel.processImageAnalysisResult(result, rotatedBitmap);
                }
            });
        }

        isAnalyzing.set(false);
        image.close();
    }

    @Override
    public void onDestroyView() {
        // [추가] TTS 리소스 해제
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}