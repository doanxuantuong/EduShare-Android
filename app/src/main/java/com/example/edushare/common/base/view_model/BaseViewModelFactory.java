package com.example.edushare.common.base.view_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Callable;
public class BaseViewModelFactory<T extends ViewModel> implements ViewModelProvider.Factory {
    private final Callable<T> creator;

    public BaseViewModelFactory(Callable<T> creator) {
        this.creator = creator;
    }

    @NonNull
    @Override
    public <VM extends ViewModel> VM create(@NonNull Class<VM> modelClass) {
        try {
            return (VM) creator.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
