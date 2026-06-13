package com.example.edushare.feature.locations;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edushare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 100;

    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText edtSearch;

    private final List<ItemLocation> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== OSMDROID FIX CỨNG =====
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        File osmdroidDir = new File(getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidTileCache(osmdroidDir);

        setContentView(R.layout.activity_map);

        // ===== MAP INIT =====
        map = findViewById(R.id.mapView);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);

        map.setBuiltInZoomControls(true); // 👈 thêm cái này để test

        GeoPoint startPoint = new GeoPoint(10.8710, 106.7932);
        map.getController().setZoom(17.0);
        map.getController().setCenter(startPoint);

        map.invalidate();

        // ===== TEST TILE LOAD =====
        map.post(() -> {
            map.getController().setZoom(18.0);
            map.invalidate();
        });

        Toast.makeText(this, "Map loading...", Toast.LENGTH_SHORT).show();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadSampleData();

        FloatingActionButton btnMyLocation = findViewById(R.id.btnMyLocation);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        edtSearch = findViewById(R.id.edtSearch);

        btnMyLocation.setOnClickListener(v -> getCurrentLocation());
        btnSearch.setOnClickListener(v -> searchItem(edtSearch.getText().toString()));
    }

    private void loadSampleData() {
        items.add(new ItemLocation("Laptop", 10.8713, 106.7930));
        items.add(new ItemLocation("Balo", 10.8709, 106.7938));
        items.add(new ItemLocation("Tai nghe", 10.8718, 106.7925));
    }

    private void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) {
                        Toast.makeText(this,
                                "Không lấy được vị trí",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showLocation(location);
                });
    }

    private void showLocation(Location location) {

        GeoPoint point = new GeoPoint(
                location.getLatitude(),
                location.getLongitude()
        );

        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle("Vị trí của bạn");

        map.getOverlays().add(marker);

        map.getController().animateTo(point);
        map.getController().setZoom(18.0);

        map.invalidate();
    }

    private void searchItem(String keyword) {

        if (keyword.isEmpty()) {
            Toast.makeText(this, "Nhập tên đồ vật", Toast.LENGTH_SHORT).show();
            return;
        }

        for (ItemLocation item : items) {

            if (item.getName().equalsIgnoreCase(keyword)) {

                GeoPoint point = new GeoPoint(
                        item.getLatitude(),
                        item.getLongitude()
                );

                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setTitle(item.getName());

                map.getOverlays().add(marker);

                map.getController().animateTo(point);
                map.getController().setZoom(19.0);

                map.invalidate();

                return;
            }
        }

        Toast.makeText(this,
                "Không tìm thấy đồ vật",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}