package com.example.edushare.feature.post.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.feature.post.model.CreateLocationRequest;
import com.example.edushare.feature.post.model.LocationDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.edushare.common.base.retrofit.RetrofitClient;

import com.example.edushare.feature.locations.network.LocationApiService;

public class PickLocationActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION_ID   = "location_id";
    public static final String EXTRA_LOCATION_NAME = "location_name";
    public static final String EXTRA_LATITUDE      = "latitude";
    public static final String EXTRA_LONGITUDE     = "longitude";

    private MapView map;
    private TextView tvInstruction;
    private EditText edtAreaName;
    private Button btnConfirm;

    private Marker pickedMarker;
    private GeoPoint pickedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        File osmdroidDir = new File(getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidTileCache(osmdroidDir);

        setContentView(R.layout.activity_pick_location);

        map           = findViewById(R.id.pickMap);
        tvInstruction = findViewById(R.id.tvInstruction);
        edtAreaName   = findViewById(R.id.edtAreaName);
        btnConfirm    = findViewById(R.id.btnConfirmLocation);

        setupMap();

        findViewById(R.id.btnPickBack).setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);

        GeoPoint nlu = new GeoPoint(10.8710, 106.7932);
        map.getController().setZoom(17.0);
        map.getController().setCenter(nlu);

        map.getOverlays().add(new org.osmdroid.views.overlay.Overlay() {
            @Override
            public boolean onSingleTapConfirmed(android.view.MotionEvent e, MapView mapView) {
                org.osmdroid.views.Projection proj = mapView.getProjection();
                GeoPoint point = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                onMapTapped(point);
                return true;
            }
        });
    }

    private void onMapTapped(GeoPoint point) {
        pickedPoint = point;

        if (pickedMarker != null) map.getOverlays().remove(pickedMarker);
        pickedMarker = new Marker(map);
        pickedMarker.setPosition(point);
        pickedMarker.setTitle("Vị trí đã chọn");
        pickedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(pickedMarker);
        map.invalidate();

        tvInstruction.setText(String.format("📍 %.5f, %.5f",
                point.getLatitude(), point.getLongitude()));

        btnConfirm.setEnabled(true);
    }

    private void confirmLocation() {
        if (pickedPoint == null) {
            Toast.makeText(this, "Hãy bấm chọn vị trí trên bản đồ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String areaName = edtAreaName.getText().toString().trim();
        if (areaName.isEmpty()) {
            edtAreaName.setError("Vui lòng nhập tên khu vực");
            edtAreaName.requestFocus();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang lưu...");

        CreateLocationRequest req = new CreateLocationRequest(
                areaName,
                pickedPoint.getLatitude(),
                pickedPoint.getLongitude()
        );

        RetrofitClient.createService(LocationApiService.class, this).createLocation(req)
                .enqueue(new Callback<LocationDto>() {
                    @Override
                    public void onResponse(Call<LocationDto> call,
                                           Response<LocationDto> response) {
                        // Thêm log để xem response
                        android.util.Log.d("PickLocation", "code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            LocationDto created = response.body();
                            android.util.Log.d("PickLocation", "id: " + created.getId());

                            Intent result = new Intent();
                            result.putExtra(EXTRA_LOCATION_ID,   created.getId());
                            result.putExtra(EXTRA_LOCATION_NAME, created.getAreaName());
                            result.putExtra(EXTRA_LATITUDE,      created.getLatitude());
                            result.putExtra(EXTRA_LONGITUDE,     created.getLongitude());
                            setResult(RESULT_OK, result);
                            finish();
                        } else {
                            Toast.makeText(PickLocationActivity.this,
                                    "Lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Xác nhận vị trí");
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationDto> call, Throwable t) {
                        android.util.Log.e("PickLocation", "onFailure: " + t.getMessage());
                        Toast.makeText(PickLocationActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Xác nhận vị trí");
                    }
                });
    }

    @Override protected void onResume() { super.onResume(); map.onResume(); }
    @Override protected void onPause()  { super.onPause();  map.onPause(); }
}
