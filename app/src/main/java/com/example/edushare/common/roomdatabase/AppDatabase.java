package com.example.edushare.common.roomdatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.edushare.feature.users.dao.UserDao;
import com.example.edushare.feature.users.entity.UserEntity;

@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
}
