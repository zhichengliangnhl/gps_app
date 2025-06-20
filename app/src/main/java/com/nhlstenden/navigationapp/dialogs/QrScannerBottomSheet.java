package com.nhlstenden.navigationapp.dialogs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.nhlstenden.navigationapp.R;
import com.nhlstenden.navigationapp.helpers.ToastUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

public class QrScannerBottomSheet extends BottomSheetDialogFragment
{
    public interface QrScanListener
    {
        void onQrScanned(String code);
    }

    private QrScanListener listener;
    private ExecutorService cameraExecutor;
    private boolean scanning = true;
    private PreviewView previewView;
    private static final String TAG = "QrScannerBottomSheet";

    public QrScannerBottomSheet(QrScanListener listener)
    {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.bottom_sheet_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.previewView = view.findViewById(R.id.previewView);
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        this.cameraExecutor = Executors.newSingleThreadExecutor();
        
        // Check if context is still valid
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot start camera");
            return;
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Camera permission granted, starting camera");
            startCamera(this.previewView);
        }
        else
        {
            Log.d(TAG, "Requesting camera permission");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
        }
    }

    private void startCamera(PreviewView previewView)
    {
        Log.d(TAG, "startCamera called");
        
        if (previewView == null) {
            Log.e(TAG, "PreviewView is null, cannot start camera");
            return;
        }
        
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot start camera");
            return;
        }
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() ->
        {
            try
            {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (cameraProvider == null) {
                    Log.e(TAG, "CameraProvider is null");
                    return;
                }
                
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new android.util.Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(this.cameraExecutor, imageProxy -> processImageProxy(imageProxy));
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                Log.d(TAG, "Camera started and surface provider set");
            } catch (ExecutionException | InterruptedException e)
            {
                Log.e(TAG, "Failed to start camera: " + e.getMessage());
                e.printStackTrace();
                // Show error to user
                if (getContext() != null) {
                    ToastUtils.show(getContext(), "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error starting camera: " + e.getMessage());
                e.printStackTrace();
                if (getContext() != null) {
                    ToastUtils.show(getContext(), "Camera error: " + e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy)
    {
        if (!this.scanning)
        {
            imageProxy.close();
            return;
        }
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null)
        {
            InputImage image = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);
            scanner.process(image)
                    .addOnSuccessListener(barcodes ->
                    {
                        for (Barcode barcode : barcodes)
                        {
                            if (barcode.getRawValue() != null)
                            {
                                this.scanning = false;
                                if (this.listener != null)
                                    this.listener.onQrScanned(barcode.getRawValue());
                                dismiss();
                                break;
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> imageProxy.close());
        }
        else
        {
            imageProxy.close();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (this.cameraExecutor != null)
            this.cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Camera permission granted in callback, starting camera");
            if (this.previewView != null)
            {
                startCamera(this.previewView);
            }
            else
            {
                Log.e(TAG, "previewView is null in onRequestPermissionsResult");
            }
        }
        else
        {
            Log.d(TAG, "Camera permission denied");
            ToastUtils.show(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT);
            dismiss();
        }
    }
}