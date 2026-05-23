package com.example.edushare.feature.users.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.edushare.feature.users.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity findByEmail(String email);
}
