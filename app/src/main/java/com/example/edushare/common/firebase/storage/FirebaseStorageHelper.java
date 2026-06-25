package com.example.edushare.common.firebase.storage;

import android.net.Uri;
import android.util.Log;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class FirebaseStorageHelper {
    private static final String TAG = "FIREBASE_STORAGE";
    private static final String STORAGE_BUCKET_URL = "gs://edushare-e8cfbf09.firebasestorage.app";
    private static final String POST_IMAGES_DIR = "post-images";

    private final FirebaseStorage storage;

    public FirebaseStorageHelper() {
        this(FirebaseStorage.getInstance(STORAGE_BUCKET_URL));
    }

    FirebaseStorageHelper(FirebaseStorage storage) {
        this.storage = storage;
    }

    public void uploadPostImage(Uri imageUri, OnActionCallback<String> callback) {
        if (imageUri == null) {
            callback.onFailure("Please choose an image first");
            return;
        }

        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg";
        StorageReference imageRef = storage.getReference()
                .child(POST_IMAGES_DIR)
                .child(fileName);

        Log.d(TAG, "Uploading post image to: " + imageRef.getPath());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    StorageReference uploadedRef = taskSnapshot.getStorage();
                    Log.d(TAG, "Upload success. Getting download URL from: " + uploadedRef.getPath());
                    uploadedRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> callback.onSuccess(downloadUri.toString()))
                            .addOnFailureListener(error -> {
                                Log.e(TAG, "Cannot get download URL", error);
                                callback.onFailure(toMessage(error));
                            });
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Cannot upload image", error);
                    callback.onFailure(toMessage(error));
                });
    }

    private String toMessage(Exception error) {
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "Cannot upload image";
        }
        return "Cannot upload image: " + message;
    }
}