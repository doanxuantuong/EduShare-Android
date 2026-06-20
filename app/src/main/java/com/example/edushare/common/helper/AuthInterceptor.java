package com.example.edushare.common.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//vào các API không phải login/register.
//Nó sẽ tự động gắn:
//Authorization: Bearer <token>

//Là một thằng đứng giữa Retrofit và Server,
// chuyên chặn mọi request trước khi gửi đi để tự gắn JWT vào header.
//Nó không tự xác thực JWT. Nó chỉ đính JWT vào request. Server mới là nơi xác thực JWT.
public class AuthInterceptor implements Interceptor {
    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // Lấy request gốc mà Retrofit sắp gửi đi.
        Request originalRequest = chain.request();

        // Lấy đường dẫn API.
        String path = originalRequest.url().encodedPath();

        // Kiểm tra API hiện tại có phải API login/register không.
        boolean isAuthApi = path.contains("/api/auth/login") || path.contains("/api/auth/register");

        // Tạo bản sao của request gốc để mình chỉnh sửa.
        // Không sửa trực tiếp request cũ, mà tạo request mới có thêm header.
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (!isAuthApi) {
            // Lấy JWT đã lưu sau khi login.
            //JWT này được lưu từ LoginActivity:
            String token = UserSessionHelper.getToken(appContext);

            if (token != null && !token.trim().isEmpty()) {
                //Gắn JWT vào header.
                // Server thường yêu cầu format như này: Authorization: Bearer <token>
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        }

        //Cho request tiếp tục gửi lên server.
        Response response = chain.proceed(requestBuilder.build());

        // Nếu server trả về 401 Unauthorized, nghĩa là:
        if (response.code() == 401) {
            UserSessionHelper.clearSession(appContext);
        }

        return response;
    }
}
