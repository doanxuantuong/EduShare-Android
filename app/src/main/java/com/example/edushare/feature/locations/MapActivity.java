package com.example.edushare.feature.locations;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edushare.R;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 100;
    // Khoảng cách (mét) để tự chuyển sang bước tiếp theo
    private static final float STEP_THRESHOLD_METERS = 15f;
    // Khoảng cách (mét) để báo "Đã đến nơi"
    private static final float ARRIVED_THRESHOLD_METERS = 10f;

    //view
    private MapView map;
    private AutoCompleteTextView edtSearch;
    private ImageButton btnSearch;
    private FloatingActionButton btnMyLocation;
    private MaterialCardView btnBack;

    // Info card (hiện sau khi search)
    private CardView infoCard;
    private TextView txtItemName, txtItemArea, txtRouteInfo;
    private Button btnStartNav;

    // Navigation card (hiện khi đang điều hướng)
    private CardView navCard;
    private TextView txtNavStep;      // "Rẽ trái sau 50m"
    private TextView txtNavDistance;  // khoảng cách còn lại đến đích
    private Button btnStopNav;

    //state
    private FusedLocationProviderClient fusedLocationClient;
    private OsrmRouteHelper osrmHelper;
    private SearchSuggestionAdapter suggestionAdapter;

    private GeoPoint myLocation;
    private Marker myMarker;
    private Marker itemMarker;
    private Polyline routePolyline;

    // Navigation state
    private boolean isNavigating = false;
    private List<GeoPoint> routePoints = new ArrayList<>();  // toàn bộ điểm route
    private List<OsrmRouteHelper.RouteStep> routeSteps = new ArrayList<>(); // các bước chỉ dẫn
    private int currentStepIndex = 0;
    private GeoPoint destination;
    private LocationCallback navLocationCallback;  // callback GPS tracking

    private List<ItemLocation> allItems = new ArrayList<>();
    private List<ItemLocation> filteredItems = new ArrayList<>();

//sample dataa
    private void loadSampleData() {
        allItems.add(new ItemLocation(1, "Laptop Dell", "Dorm A",
                10.8715, 106.7928, "Laptop Dell 15 inch màu đen", ""));
        allItems.add(new ItemLocation(2, "Balo Nike", "Dorm A",
                10.8712, 106.7931, "Balo Nike màu xanh", ""));
        allItems.add(new ItemLocation(3, "Tai nghe Sony", "Dorm B",
                10.8720, 106.7940, "Tai nghe Sony WH-1000XM4", ""));
        allItems.add(new ItemLocation(4, "Sạc dự phòng", "Dorm B",
                10.8718, 106.7943, "Pin dự phòng 20000mAh", ""));
        allItems.add(new ItemLocation(5, "Xe đạp", "Canteen",
                10.8708, 106.7925, "Xe đạp gấp màu đỏ", ""));
        allItems.add(new ItemLocation(6, "Áo mưa", "Canteen",
                10.8705, 106.7922, "Áo mưa 2 lớp size M", ""));
        allItems.add(new ItemLocation(7, "Giày thể thao", "Library",
                10.8722, 106.7935, "Nike Air Force 1 size 42", ""));
        allItems.add(new ItemLocation(8, "Cặp sách", "Library",
                10.8725, 106.7938, "Cặp da màu nâu", ""));
    }

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

        setContentView(R.layout.activity_map);

        bindViews();
        setupMap();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        osrmHelper = new OsrmRouteHelper();

        loadSampleData();
        filteredItems = new ArrayList<>(allItems);

        setupSearch();

        btnMyLocation.setOnClickListener(v -> getCurrentLocation());
        btnSearch.setOnClickListener(v -> {
            String kw = edtSearch.getText().toString().trim();
            if (!kw.isEmpty()) searchAndNavigate(kw);
        });

        // Nút Bắt đầu → bắt đầu navigation
        btnStartNav.setOnClickListener(v -> startNavigation());

        // Nút Dừng → thoát navigation
        btnStopNav.setOnClickListener(v -> stopNavigation());
    }

    // bind view
    private void bindViews() {
        map           = findViewById(R.id.mapView);
        edtSearch     = findViewById(R.id.edtSearch);
        btnSearch     = findViewById(R.id.btnSearch);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnBack       = findViewById(R.id.btnBack);


        infoCard      = findViewById(R.id.infoCard);
        txtItemName   = findViewById(R.id.txtItemName);
        txtItemArea   = findViewById(R.id.txtItemArea);
        txtRouteInfo  = findViewById(R.id.txtRouteInfo);
        btnStartNav   = findViewById(R.id.btnStartNav);

        navCard       = findViewById(R.id.navCard);
        txtNavStep    = findViewById(R.id.txtNavStep);
        txtNavDistance = findViewById(R.id.txtNavDistance);
        btnStopNav    = findViewById(R.id.btnStopNav);
    }

    //map
    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.setBuiltInZoomControls(false);
        GeoPoint nlu = new GeoPoint(10.8710, 106.7932);
        map.getController().setZoom(17.0);
        map.getController().setCenter(nlu);
    }

    // search
    private void setupSearch() {
        suggestionAdapter = new SearchSuggestionAdapter(this, filteredItems);
        edtSearch.setAdapter(suggestionAdapter);
        edtSearch.setThreshold(1);

        edtSearch.setOnItemClickListener((parent, view, position, id) -> {
            ItemLocation item = suggestionAdapter.getItem(position);
            if (item != null) focusItem(item);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 0) clearSearchResult();
            }
        });
    }

    private void clearSearchResult() {
        if (isNavigating) stopNavigation();
        if (itemMarker != null) { map.getOverlays().remove(itemMarker); itemMarker = null; }
        clearRoute();
        infoCard.setVisibility(View.GONE);
        map.invalidate();
    }

    // focus item sau khi search
    private void focusItem(ItemLocation item) {
        if (isNavigating) stopNavigation();

        infoCard.setVisibility(View.VISIBLE);
        txtItemName.setText(item.getName());
        txtItemArea.setText(item.getAreaName() + " • " + item.getDescription());
        txtRouteInfo.setText("Đang tính đường đi...");
        btnStartNav.setVisibility(View.GONE); // ẩn nút cho đến khi có route

        destination = new GeoPoint(item.getLatitude(), item.getLongitude());

        if (itemMarker != null) map.getOverlays().remove(itemMarker);
        itemMarker = new Marker(map);
        itemMarker.setPosition(destination);
        itemMarker.setTitle(item.getName());
        itemMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(itemMarker);
        itemMarker.showInfoWindow();

        if (myLocation != null) {
            drawRoute(myLocation, destination);
        } else {
            txtRouteInfo.setText("Bật GPS để xem đường đi");
            map.getController().animateTo(destination);
            map.getController().setZoom(19.0);
        }
        map.invalidate();
    }

    private void searchAndNavigate(String keyword) {
        for (ItemLocation item : filteredItems) {
            if (item.getName().toLowerCase(Locale.ROOT)
                    .contains(keyword.toLowerCase(Locale.ROOT))) {
                focusItem(item);
                return;
            }
        }
        Toast.makeText(this, "Không tìm thấy: " + keyword, Toast.LENGTH_SHORT).show();
    }

    // vẽ đường
    private void drawRoute(GeoPoint from, GeoPoint to) {
        clearRoute();
        osrmHelper.fetchRoute(from, to, new OsrmRouteHelper.RouteCallback() {
            @Override
            public void onRouteReady(List<GeoPoint> points,
                                     List<OsrmRouteHelper.RouteStep> steps,
                                     double distanceMeters,
                                     double durationSeconds) {
                routePoints = points;
                routeSteps  = steps;

                routePolyline = new Polyline();
                routePolyline.setPoints(points);
                routePolyline.getOutlinePaint().setColor(Color.parseColor("#2196F3"));
                routePolyline.getOutlinePaint().setStrokeWidth(12f);
                routePolyline.getOutlinePaint().setAlpha(200);
                map.getOverlays().add(0, routePolyline);
                map.invalidate();

                BoundingBox box = BoundingBox.fromGeoPoints(List.of(from, to));
                map.zoomToBoundingBox(box.increaseByScale(1.4f), true, 120);

                String dist = distanceMeters >= 1000
                        ? String.format(Locale.ROOT, "%.1f km", distanceMeters / 1000)
                        : String.format(Locale.ROOT, "%.0f m", distanceMeters);
                int mins = (int) (durationSeconds / 60);
                txtRouteInfo.setText("📍 " + dist + "  •  🕐 " + mins + " phút đi bộ");

                // Hiện nút Bắt đầu khi đã có route
                btnStartNav.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                txtRouteInfo.setText("Không thể tải đường đi");
                Toast.makeText(MapActivity.this,
                        "Lỗi route: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearRoute() {
        if (routePolyline != null) {
            map.getOverlays().remove(routePolyline);
            routePolyline = null;
            map.invalidate();
        }
    }

    // NAVIGATION

    private void startNavigation() {
        if (myLocation == null) {
            Toast.makeText(this, "Cần bật GPS trước!", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }
        if (routeSteps.isEmpty()) {
            Toast.makeText(this, "Chưa có đường đi!", Toast.LENGTH_SHORT).show();
            return;
        }

        isNavigating = true;
        currentStepIndex = 0;

        // Ẩn info card, hiện nav card
        infoCard.setVisibility(View.GONE);
        navCard.setVisibility(View.VISIBLE);

        // Hiện bước đầu tiên
        showCurrentStep();

        // Bắt đầu tracking GPS liên tục (mỗi 2 giây)
        startLocationTracking();
    }

    private void stopNavigation() {
        isNavigating = false;
        stopLocationTracking();
        navCard.setVisibility(View.GONE);
        // Hiện lại info card nếu còn destination
        if (destination != null) infoCard.setVisibility(View.VISIBLE);
        // Reset zoom nhìn toàn route
        if (myLocation != null && destination != null) {
            BoundingBox box = BoundingBox.fromGeoPoints(List.of(myLocation, destination));
            map.zoomToBoundingBox(box.increaseByScale(1.4f), true, 120);
        }
    }

    /** Hiện chỉ dẫn bước hiện tại lên navCard */
    private void showCurrentStep() {
        if (currentStepIndex >= routeSteps.size()) return;
        OsrmRouteHelper.RouteStep step = routeSteps.get(currentStepIndex);

        // Icon hướng + text
        String icon = directionIcon(step.maneuver);
        txtNavStep.setText(icon + "  " + step.instruction);

        // Khoảng cách đến đích
        if (destination != null && myLocation != null) {
            float[] result = new float[1];
            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    destination.getLatitude(), destination.getLongitude(),
                    result
            );
            String dist = result[0] >= 1000
                    ? String.format(Locale.ROOT, "%.1f km đến nơi", result[0] / 1000)
                    : String.format(Locale.ROOT, "%.0f m đến nơi", result[0]);
            txtNavDistance.setText(dist);
        }
    }

    /** Map maneuver type → emoji icon */
    private String directionIcon(String maneuver) {
        if (maneuver == null) return "⬆️";
        switch (maneuver) {
            case "turn-left":        return "⬅️";
            case "turn-right":       return "➡️";
            case "turn-sharp-left":  return "↩️";
            case "turn-sharp-right": return "↪️";
            case "turn-slight-left": return "↖️";
            case "turn-slight-right":return "↗️";
            case "uturn":            return "🔄";
            case "arrive":           return "📍";
            default:                 return "⬆️";
        }
    }

    // ── GPS tracking liên tục khi navigation ──────────────────────────────
    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(2000) // mỗi 2 giây
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(1000)
                .build();

        navLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || !isNavigating) return;
                Location loc = result.getLastLocation();
                if (loc == null) return;
                onNavLocationUpdate(loc);
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest, navLocationCallback, Looper.getMainLooper());
    }

    private void stopLocationTracking() {
        if (navLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(navLocationCallback);
            navLocationCallback = null;
        }
    }

    /** Gọi mỗi khi GPS cập nhật trong navigation mode */
    private void onNavLocationUpdate(Location location) {
        myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Cập nhật marker user
        if (myMarker != null) map.getOverlays().remove(myMarker);
        myMarker = new Marker(map);
        myMarker.setPosition(myLocation);
        myMarker.setTitle("Bạn đang ở đây");
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myMarker);

        // Camera theo user
        map.getController().animateTo(myLocation);
        map.getController().setZoom(19.0);
        map.invalidate();

        // Kiểm tra đã đến đích chưa
        if (destination != null) {
            float[] distToDest = new float[1];
            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    destination.getLatitude(), destination.getLongitude(),
                    distToDest
            );
            if (distToDest[0] <= ARRIVED_THRESHOLD_METERS) {
                onArrived();
                return;
            }
        }

        // Kiểm tra đã qua bước hiện tại chưa → chuyển bước tiếp
        if (currentStepIndex < routeSteps.size()) {
            OsrmRouteHelper.RouteStep step = routeSteps.get(currentStepIndex);
            if (step.location != null) {
                float[] distToStep = new float[1];
                Location.distanceBetween(
                        myLocation.getLatitude(), myLocation.getLongitude(),
                        step.location.getLatitude(), step.location.getLongitude(),
                        distToStep
                );
                if (distToStep[0] <= STEP_THRESHOLD_METERS) {
                    currentStepIndex++;
                    if (currentStepIndex < routeSteps.size()) {
                        showCurrentStep();
                    }
                }
            }
        }

        // Cập nhật khoảng cách còn lại
        showCurrentStep();
    }

    private void onArrived() {
        stopLocationTracking();
        isNavigating = false;
        navCard.setVisibility(View.GONE);
        Toast.makeText(this, "🎉 Đã đến nơi!", Toast.LENGTH_LONG).show();
        // Hiện lại marker đích nổi bật
        if (itemMarker != null) itemMarker.showInfoWindow();
        map.invalidate();
    }

    //gps lần đầu
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
        fusedLocationClient.getCurrentLocation(request, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) onLocationReceived(location);
                    else Toast.makeText(this, "Không lấy được vị trí. Hãy bật GPS.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void onLocationReceived(Location location) {
        myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (myMarker != null) map.getOverlays().remove(myMarker);
        myMarker = new Marker(map);
        myMarker.setPosition(myLocation);
        myMarker.setTitle("Vị trí của bạn");
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myMarker);
        map.getController().animateTo(myLocation);
        map.getController().setZoom(18.0);
        map.invalidate();

        Toast.makeText(this, "Đã cập nhật vị trí!", Toast.LENGTH_SHORT).show();

        if (itemMarker != null && itemMarker.getPosition() != null) {
            drawRoute(myLocation, itemMarker.getPosition());
        }
    }

    //permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Cần cấp quyền vị trí.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onResume()  { super.onResume();  map.onResume(); }
    @Override protected void onPause()   { super.onPause();   map.onPause(); }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
        osrmHelper.shutdown();
    }
}