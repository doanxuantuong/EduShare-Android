package com.example.edushare.feature.notification.view_model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.edushare.feature.notification.NotificationRepository;

public class NotificationViewModelFactory implements ViewModelProvider.Factory {

    private final NotificationRepository repository;

    public NotificationViewModelFactory(NotificationRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
            return (T) new NotificationViewModel(repository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
