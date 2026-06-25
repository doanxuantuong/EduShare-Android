package com.example.edushare.feature.auth.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.edushare.common.base.retrofit.OnActionCallback;
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
    private boolean isProcessing = false;

    private String extractStudentCode(String text) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            Matcher matcher = Pattern.compile("\\d{8}").matcher(line);
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
                    if (candidate.isEmpty() || candidate.matches(".*\\d.*")) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        // Ánh xạ View
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        tvResult = findViewById(R.id.tvResult);

        // Kiểm tra quyền Camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        btnCapture.setOnClickListener(v -> captureAndRecognizeText());

        // Vẽ khung ngắm định vị chữ nhật bo góc
        View viewOverlay = findViewById(R.id.viewOverlay);
        viewOverlay.post(() -> {
            int width = viewOverlay.getWidth();
            int height = viewOverlay.getHeight();

            int rectWidth = (int) (width * 0.85);
            int rectHeight = (int) (rectWidth * 0.60);

            int left = (width - rectWidth) / 2;
            int top = (height - rectHeight) / 2;
            int right = left + rectWidth;
            int bottom = top + rectHeight;

            android.graphics.RectF rectCard = new android.graphics.RectF(left, top, right, bottom);
            android.graphics.drawable.Drawable drawable = new android.graphics.drawable.Drawable() {
                @Override
                public void draw(@NonNull android.graphics.Canvas canvas) {
                    android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

                    // 1. Vẽ lớp phủ mờ màu đen toàn màn hình camera
                    paint.setColor(android.graphics.Color.parseColor("#99000000"));
                    paint.setStyle(android.graphics.Paint.Style.FILL);
                    canvas.drawRect(0, 0, width, height, paint);

                    // 2. Đục lỗ một hình chữ nhật bo góc ở giữa
                    paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
                    canvas.drawRoundRect(rectCard, 24, 24, paint);
                    paint.setXfermode(null);

                    // 3. Vẽ viền nét đứt màu trắng bo quanh ô ngắm
                    paint.setColor(android.graphics.Color.WHITE);
                    paint.setStyle(android.graphics.Paint.Style.STROKE);
                    paint.setStrokeWidth(6f);
                    paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{20, 10}, 0));
                    canvas.drawRoundRect(rectCard, 24, 24, paint);
                }

                @Override
                public void setAlpha(int alpha) {}
                @Override
                public void setColorFilter(@androidx.annotation.Nullable android.graphics.ColorFilter colorFilter) {}
                @Override
                public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
            };
            viewOverlay.setBackground(drawable);
        });
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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
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
            tvResult.setText("Camera chưa sẵn sàng, vui lòng thử lại.");
            return;
        }

        isProcessing = true;
        runOnUiThread(() -> tvResult.setText("Đang quét dữ liệu, vui lòng đợi..."));

        imageCapture.takePicture(
                cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        try {
                            InputImage image = InputImage.fromMediaImage(
                                    imageProxy.getImage(),
                                    imageProxy.getImageInfo().getRotationDegrees()
                            );

                            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                    .process(image)
                                    .addOnSuccessListener(text -> {
                                        String result = text.getText();
                                        String studentCode = extractStudentCode(result);
                                        String fullName = extractFullName(result);

                                        if (studentCode.isEmpty() || fullName.isEmpty()) {
                                            runOnUiThread(() -> tvResult.setText("Không đọc được thông tin, vui lòng căn chỉnh lại thẻ."));
                                            imageProxy.close();
                                            isProcessing = false;
                                            return;
                                        }

                                        String email = studentCode + "@st.hcmuaf.edu.vn";

                                        OcrLoginRequest request = new OcrLoginRequest();
                                        request.setStudentCode(studentCode);
                                        request.setFullName(fullName);
                                        request.setEmail(email);

                                        new UserRepository(OcrActivity.this).loginUserByOcr(request, new OnActionCallback<LoginResponse>() {
                                            @Override
                                            public void onSuccess(LoginResponse response) {
                                                runOnUiThread(() -> {
                                                    UserSessionHelper.saveSession(
                                                            OcrActivity.this,
                                                            response.getToken(),
                                                            response.getUserId(),
                                                            response.getFullName(),
                                                            response.getEmail()
                                                    );
                                                    Intent intent = new Intent(OcrActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                            }

                                            @Override
                                            public void onFailure(String message) {
                                                runOnUiThread(() -> tvResult.setText(message));
                                            }
                                        });

                                        imageProxy.close();
                                        isProcessing = false;
                                    })
                                    .addOnFailureListener(e -> {
                                        runOnUiThread(() -> tvResult.setText("Không đọc được thông tin, vui lòng thử lại."));
                                        imageProxy.close();
                                        isProcessing = false;
                                    });
                        } catch (Exception e) {
                            imageProxy.close();
                            isProcessing = false;
                            runOnUiThread(() -> tvResult.setText("Có lỗi xảy ra trong quá trình quét."));
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> tvResult.setText("Không thể chụp ảnh, vui lòng thử lại."));
                        isProcessing = false;
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}