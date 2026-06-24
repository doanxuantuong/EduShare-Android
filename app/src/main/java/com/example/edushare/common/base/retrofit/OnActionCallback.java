package com.example.edushare.common.base.retrofit;

public interface OnActionCallback<T> {
    void onSuccess(T data);
    void onFailure(String error);
}
