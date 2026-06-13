package com.example.edushare.common.base;

public interface OnActionCallback<T> {
    void onSuccess(T data);
    void onFailure(String error);
}
