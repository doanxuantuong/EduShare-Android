package com.example.edushare.data.local.roomdatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.edushare.data.local.dao.UserDao;
import com.example.edushare.data.local.entity.UserEntity;

@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
}
