package com.example.edushare.common.base;

import android.content.Context;

import com.example.edushare.common.helper.AuthInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    //Test OCR cái 172....2 thì sẽ đổi theo mạng
//    private static final String BASE_URL = "http://172.20.10.2:8080/";
    private static Retrofit retrofit = null;

    //ĐÂY LÀ 1 DẠNG DYNAMIC PROXY
    public static <T> T createService(Class<T> serviceClass, Context context) {
        if (retrofit == null) {
            // Tạo logging để soi JSON
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Xem cả nội dung bên trong

            //Đây là thằng vận chuyển dữ liệu
            //Không có nó thì sẽ dùng 1 cái mặc định
            //Do muốn có logging (HttpLoggingInterceptor) nên tạo ra để thêm HttpLoggingInterceptor
            //1 cái OkHttpClient sẽ không có logging
            OkHttpClient client = new OkHttpClient.Builder()
                    // Tự động gắn JWT vào request
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))

                    .addInterceptor(logging)
                    .build();

            //Tạo ra đường dẫn kết nối đến với đường dẫn cha là BASE_URL
            //Kèm với Logging để xem hành trình
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        //Retrofit sử dụng kỹ thuật Dynamic Proxy
        //để tạo ra một đối tượng từ UserApi trong lúc App đang chạy.
        //Lúc bên repository thì cứ gọi phương thức có trong interface UserApi
        //Nó sẽ map phương thức đó đến API bên Spring Boot
        return retrofit.create(serviceClass);
    }

}