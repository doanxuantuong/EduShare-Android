package com.example.edushare.feature.users;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.roomdatabase.AppDatabase;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.users.entity.UserEntity;

public class LocalUserRepository {

    private final AppDatabase db;

    public LocalUserRepository(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "edushare_db").allowMainThreadQueries().build();
    }

    public void register(RegisterRequest user, String password, OnActionCallback callback) {
        UserEntity existingUser = db.userDao().findByEmail(user.getEmail());

        // check email tồn tại
        if(existingUser != null) {
            callback.onFailure("Email đã tồn tại");
            return;
        }

        // tạo entity
        UserEntity userEntity = new UserEntity(user.getEmail(), user.getFullName(), user.getStudentCode(),
                user.getPhone(), password);

        // insert Room
        db.userDao().insert(userEntity);
        callback.onSuccess("Đăng ký thành công");
    }
}
