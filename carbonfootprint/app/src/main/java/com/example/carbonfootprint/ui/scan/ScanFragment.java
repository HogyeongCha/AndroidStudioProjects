package com.example.carbonfootprint.ui.scan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.Image;
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
import androidx.navigation.fragment.NavHostFragment;

import com.example.carbonfootprint.R;
import com.example.carbonfootprint.ui.scan.ScanFragmentDirections;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment"; // 로그 확인을 위한 태그
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private BarcodeScanner barcodeScanner;
    // 중복 스캔 및 화면 전환을 방지하기 위한 플래그
    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    // 권한 요청 결과를 처리하는 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        checkCameraPermission();
    }

    /**
     * [수정된 부분]
     * 프래그먼트가 화면에 다시 나타날 때마다 호출됩니다.
     * 여기서 스캔 상태 플래그를 초기화하여, '새로 스캔하기'로 돌아왔을 때
     * 다시 바코드 인식이 가능하도록 만듭니다.
     */
    @Override
    public void onResume() {
        super.onResume();
        isScanning.set(false);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);


        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeImage(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty() && isScanning.compareAndSet(false, true)) {
                            Barcode barcode = barcodes.get(0);
                            String barcodeValue = barcode.getRawValue();
                            Log.d(TAG, "Barcode detected: " + barcodeValue);

                            requireActivity().runOnUiThread(() -> {
                                ScanFragmentDirections.ActionScanFragmentToResultFragment action =
                                        ScanFragmentDirections.actionScanFragmentToResultFragment(barcodeValue);
                                NavHostFragment.findNavController(ScanFragment.this).navigate(action);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Barcode scanning failed", e);
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}