package com.example.edushare.feature.post.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.feature.post.helper.PostItemFormHelper;

public class PostItemActivity extends AppCompatActivity {
    private PostItemFormHelper postItemFormHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);

        // Giữ Activity chỉ xử lý vòng đời; phần gắn sự kiện UI của form nằm trong Helper.
        postItemFormHelper = new PostItemFormHelper(this);
        postItemFormHelper.setup();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            int locationId      = data.getIntExtra(PickLocationActivity.EXTRA_LOCATION_ID, -1);
            String locationName = data.getStringExtra(PickLocationActivity.EXTRA_LOCATION_NAME);
            postItemFormHelper.onLocationPicked(locationId, locationName);
        }
    }
}