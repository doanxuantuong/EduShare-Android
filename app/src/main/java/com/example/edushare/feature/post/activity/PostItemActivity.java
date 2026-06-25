package com.example.edushare.feature.post.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.feature.post.helper.PostItemFormHelper;

public class PostItemActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_LOCATION = 200;
    private static final int REQUEST_PICK_PHOTO = 201;

    private PostItemFormHelper postItemFormHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);

        postItemFormHelper = new PostItemFormHelper(this, REQUEST_PICK_PHOTO);
        postItemFormHelper.setup();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_LOCATION && resultCode == RESULT_OK && data != null) {
            int locationId = data.getIntExtra(PickLocationActivity.EXTRA_LOCATION_ID, -1);
            String locationName = data.getStringExtra(PickLocationActivity.EXTRA_LOCATION_NAME);
            postItemFormHelper.onLocationPicked(locationId, locationName);
            return;
        }

        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            postItemFormHelper.onPhotoPicked(data.getData());
        }
    }
}