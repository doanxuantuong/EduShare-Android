package com.example.edushare.feature.auth.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.AuthManager;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.auth.request.OcrLoginRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.users.UserRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrActivity extends AppCompatActivity {

    private PreviewView previewView;
    private Button btnCapture;
    private TextView tvResult;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private static final String TAG = "OcrActivity_Debug";

    private String extractStudentCode(String text) {

        String[] lines = text.split("\\n");

        for (String line : lines) {

            Matcher matcher =
                    Pattern.compile("\\d{8}").matcher(line);

            if (matcher.find()) {
                return matcher.group();
            }
        }

        return "";
    }

    private String extractFullName(String text) {

        String[] lines = text.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches(".*\\d{8}.*")) {
                for (int j = i - 1; j >= 0; j--) {
                    String candidate = lines[j].trim();

                    if (candidate.isEmpty()) {
                        continue;
                    }

                    if (candidate.matches(".*\\d.*")) {
                        continue;
                    }

                    if (candidate.toLowerCase().contains("sinh viên")
                            || candidate.toLowerCase().contains("đại học")
                            || candidate.toLowerCase().contains("nông lâm")
                            || candidate.toLowerCase().contains("hồ chí minh")
                            || candidate.toLowerCase().contains("bidv")) {
                        continue;
                    }

                    return candidate;
                }
            }
        }

        return "";
    }

    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        tvResult = findViewById(R.id.tvResult);

        cameraExecutor = Executors.newSingleThreadExecutor();

        startCamera();

        btnCapture.setOnClickListener(v -> captureAndRecognizeText());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                tvResult.setText("Bạn cần cấp quyền camera để quét OCR");
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                imageCapture = new ImageCapture.Builder().build();

                androidx.camera.core.Preview preview =
                        new androidx.camera.core.Preview.Builder().build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );


            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndRecognizeText() {
        if (isProcessing) {
            Log.d(TAG, "Đang xử lý, bỏ qua lượt bấm này.");
            return;
        }

        if (imageCapture == null) {
            Log.e(TAG, "Lỗi: Camera chưa sẵn sàng (imageCapture là null)");
            tvResult.setText("Camera chưa sẵn sàng");
            return;
        }

        isProcessing = true;
        Log.d(TAG, "1. Bắt đầu chụp ảnh...");

        imageCapture.takePicture(
                cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {

                    @OptIn(markerClass = ExperimentalGetImage.class)
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Log.d(TAG, "2. Chụp ảnh thành công! Đang đưa vào ML Kit...");

                        try {
                            InputImage image = InputImage.fromMediaImage(
                                    imageProxy.getImage(),
                                    imageProxy.getImageInfo().getRotationDegrees()
                            );

                            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                    .process(image)
                                    .addOnSuccessListener(text -> {
                                        String result = text.getText();
                                        Log.d(TAG, "3. ML Kit đọc xong chữ. Raw text:\n" + result);

                                        String studentCode = extractStudentCode(result);
                                        String fullName = extractFullName(result);

                                        Log.d(TAG, "4. Kết quả trích xuất -> MSSV: [" + studentCode + "], Tên: [" + fullName + "]");

                                        if (studentCode.isEmpty()) {
                                            Log.w(TAG, "Cảnh báo: Không đọc được MSSV từ ảnh!");
                                            runOnUiThread(() -> tvResult.setText("Không đọc được MSSV"));
                                            imageProxy.close();
                                            isProcessing = false;
                                            return;
                                        }

                                        String email = studentCode + "@st.hcmuaf.edu.vn";

                                        // Cập nhật giao diện (vẫn chạy bình thường)
                                        runOnUiThread(() -> {
                                            tvResult.setText(
                                                    "MSSV = " + studentCode +
                                                            "\nTên = " + fullName +
                                                            "\nEmail = " + email +
                                                            "\n\nOCR:\n" + result
                                            );
                                        });

                                        OcrLoginRequest request = new OcrLoginRequest();
                                        request.setStudentCode(studentCode);
                                        request.setFullName(fullName);
                                        request.setEmail(email);

                                        Log.d(TAG, "5. Bắt đầu gọi API tới Server với MSSV: " + studentCode);

                                        new UserRepository(OcrActivity.this).loginUserByOcr(request, new OnActionCallback<LoginResponse>() {
                                            @Override
                                            public void onSuccess(LoginResponse response) {
                                                Log.d(TAG, "6. API trả về THÀNH CÔNG! Token: " + response.getToken());

                                                runOnUiThread(() -> {
                                                    Log.d(TAG, "7. Đang chạy Main Thread, chuẩn bị chuyển trang HomeActivity...");
                                                    UserSessionHelper.saveSession(
                                                            OcrActivity.this,
                                                            response.getToken(),
                                                            response.getUserId(),
                                                            response.getFullName(),
                                                            response.getEmail()
                                                    );

                                                    Intent intent = new Intent(OcrActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    Log.d(TAG, "8. Đã gọi lệnh startActivity(intent)");
                                                    finish();
                                                });
                                            }

                                            @Override
                                            public void onFailure(String message) {
                                                Log.e(TAG, "6. API trả về THẤT BẠI! Lỗi: " + message);
                                                runOnUiThread(() -> tvResult.setText(message));
                                            }
                                        });

                                        imageProxy.close();
                                        isProcessing = false;
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi: ML Kit nhận diện OCR thất bại: ", e);
                                        runOnUiThread(() -> tvResult.setText("OCR thất bại"));
                                        imageProxy.close();
                                        isProcessing = false;
                                    });

                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi Exception trong lúc xử lý ảnh: ", e);
                            imageProxy.close();
                            isProcessing = false;
                            runOnUiThread(() -> tvResult.setText("Có lỗi xảy ra"));
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Lỗi: Không thể chụp ảnh từ Camera: ", exception);
                        runOnUiThread(() -> tvResult.setText("Không thể chụp ảnh"));
                        isProcessing = false;
                    }
                }
        );
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}

