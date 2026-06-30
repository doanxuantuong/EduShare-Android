package com.example.edushare.feature.locations.activity;

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
import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.feature.locations.adapter.SearchSuggestionAdapter;
import com.example.edushare.feature.locations.helper.*;
import com.example.edushare.feature.locations.model.PostMapDto;
import com.example.edushare.feature.locations.network.LocationApiService;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.example.edushare.feature.locations.model.ItemLocation;
import android.content.Intent;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;
import com.example.edushare.feature.locations.helper.AreaBoundaryHelper;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 100;
    private static final float STEP_THRESHOLD_METERS = 15f;
    private static final float ARRIVED_THRESHOLD_METERS = 10f;

    // Views
    private MapView map;
    private AutoCompleteTextView edtSearch;
    private ImageButton btnSearch, btnBack;
    private FloatingActionButton btnMyLocation;

    private String selectedArea = "All";
    private ChipGroup chipGroupAreas;

    // Info card
    private CardView infoCard;
    private TextView txtItemName, txtItemArea, txtRouteInfo;
    private Button btnStartNav;

    // Navigation card
    private CardView navCard;
    private TextView txtNavStep;
    private TextView txtNavDistance;
    private Button btnStopNav;

    // State
    private FusedLocationProviderClient fusedLocationClient;
    private OsrmRouteHelper osrmHelper;
    private SearchSuggestionAdapter suggestionAdapter;

    private GeoPoint myLocation;
    private Marker myMarker;
    private Marker itemMarker;
    private final List<Marker> searchMarkers = new ArrayList<>();
    private Polyline routePolyline;

    // Navigation state
    private boolean isNavigating = false;
    private List<GeoPoint> routePoints = new ArrayList<>();
    private List<OsrmRouteHelper.RouteStep> routeSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private GeoPoint destination;
    private LocationCallback navLocationCallback;
    private View footerLayout;

   private final List<ItemLocation> allItems = new ArrayList<>();

   private boolean userInteracting = false;
    private int currentPostId = -1;
    private AreaBoundaryHelper areaBoundaryHelper;

    // Sample data
//    private void loadSampleData() {
//        // Dorm A
//        allItems.add(new ItemLocation(1, "Laptop Dell", "Dorm A",
//                10.8715, 106.7928, "Laptop Dell 15 inch màu đen", ""));
//        allItems.add(new ItemLocation(2, "Laptop Asus", "Dorm A",
//                10.8714, 106.7929, "Laptop Asus VivoBook", ""));
//        allItems.add(new ItemLocation(3, "Balo Nike", "Dorm A",
//                10.8712, 106.7931, "Balo Nike màu xanh", ""));
//        allItems.add(new ItemLocation(4, "Tai nghe Sony", "Dorm A",
//                10.8713, 106.7930, "Tai nghe chống ồn", ""));
//
//        // Dorm B
//        allItems.add(new ItemLocation(5, "Laptop HP", "Dorm B",
//                10.8720, 106.7940, "Laptop HP Pavilion", ""));
//        allItems.add(new ItemLocation(6, "Laptop Dell", "Dorm B",
//                10.8719, 106.7941, "Laptop Dell Inspiron", ""));
//        allItems.add(new ItemLocation(7, "Tai nghe Sony", "Dorm B",
//                10.8721, 106.7942, "Sony WH-1000XM4", ""));
//        allItems.add(new ItemLocation(8, "Sạc dự phòng", "Dorm B",
//                10.8718, 106.7943, "Pin dự phòng 20000mAh", ""));
//
//        // Canteen
//        allItems.add(new ItemLocation(9, "Xe đạp", "Canteen",
//                10.8708, 106.7925, "Xe đạp gấp màu đỏ", ""));
//        allItems.add(new ItemLocation(10, "Laptop Lenovo", "Canteen",
//                10.8707, 106.7926, "Laptop Lenovo ThinkPad", ""));
//        allItems.add(new ItemLocation(11, "Áo mưa", "Canteen",
//                10.8705, 106.7922, "Áo mưa 2 lớp size M", ""));
//        allItems.add(new ItemLocation(12, "Balo Adidas", "Canteen",
//                10.8706, 106.7924, "Balo Adidas màu đen", ""));
//
//        // Library
//        allItems.add(new ItemLocation(13, "Laptop Acer", "Library",
//                10.8722, 106.7935, "Laptop Acer Aspire", ""));
//        allItems.add(new ItemLocation(14, "Laptop Dell", "Library",
//                10.8723, 106.7936, "Laptop Dell Latitude", ""));
//        allItems.add(new ItemLocation(15, "Giày thể thao", "Library",
//                10.8724, 106.7937, "Nike Air Force 1 size 42", ""));
//        allItems.add(new ItemLocation(16, "Cặp sách", "Library",
//                10.8725, 106.7938, "Cặp da màu nâu", ""));
//
//        // Hall A
//        allItems.add(new ItemLocation(17, "Laptop MacBook", "Hall A",
//                10.8730, 106.7930, "MacBook Air M2", ""));
//        allItems.add(new ItemLocation(18, "Tai nghe AirPods", "Hall A",
//                10.8731, 106.7931, "AirPods Pro", ""));
//        allItems.add(new ItemLocation(19, "Sạc dự phòng", "Hall A",
//                10.8732, 106.7932, "Pin dự phòng Anker", ""));
//
//        // Hall B
//        allItems.add(new ItemLocation(20, "Laptop Dell", "Hall B",
//                10.8735, 106.7945, "Dell XPS 13", ""));
//        allItems.add(new ItemLocation(21, "Balo Nike", "Hall B",
//                10.8736, 106.7946, "Balo Nike thể thao", ""));
//        allItems.add(new ItemLocation(22, "Tai nghe Sony", "Hall B",
//                10.8737, 106.7947, "Sony XB910N", ""));
//    }

    //API
    private void loadPostsFromApi() {
        RetrofitClient.createService(LocationApiService.class, this).getPostsForMap(null, null)
                .enqueue(new retrofit2.Callback<List<PostMapDto>>() {

                    @Override
                    public void onResponse(retrofit2.Call<List<PostMapDto>> call,
                                           retrofit2.Response<List<PostMapDto>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(MapActivity.this,
                                    "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        allItems.clear();
                        for (PostMapDto dto : response.body()) {
                            allItems.add(dto.toItemLocation());
                        }

                        // Refresh UI trên main thread (đã ở main thread do Retrofit)
                        suggestionAdapter.updateData(allItems);
                        setupAreaFilters();

                        Toast.makeText(MapActivity.this,
                                "Đã tải " + allItems.size() + " items", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<PostMapDto>> call, Throwable t) {
                        Toast.makeText(MapActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    //Lifecycle
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

        btnBack.setOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        osrmHelper = new OsrmRouteHelper();

//        loadSampleData();
        loadPostsFromApi();

        // Nhận tọa độ từ PostDetailActivity nếu có
        int targetPostId = getIntent().getIntExtra("POST_ID", -1);
        double targetLat = getIntent().getDoubleExtra("TARGET_LAT", 0);
        double targetLng = getIntent().getDoubleExtra("TARGET_LNG", 0);
        String targetName = getIntent().getStringExtra("TARGET_NAME");

        currentPostId = targetPostId;
        if (targetLat != 0 && targetLng != 0) {
            GeoPoint target = new GeoPoint(targetLat, targetLng);

            destination = target;

            map.getController().setCenter(target);
            map.getController().setZoom(18.0);

            Marker marker = new Marker(map);
            marker.setPosition(target);
            marker.setTitle(targetName != null ? targetName : "Vị trí");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
            marker.showInfoWindow();

            itemMarker = marker;

            infoCard.setVisibility(View.VISIBLE);
            txtItemName.setText(targetName != null ? targetName : "Vị trí sản phẩm");
            txtItemArea.setText("Vị trí từ bài đăng");
            txtRouteInfo.setText("Bật GPS để xem đường đi");

            getCurrentLocation();

            map.invalidate();
        }

        setupSearch();
        setupAreaFilters();

        btnMyLocation.setOnClickListener(v -> getCurrentLocation());
        btnSearch.setOnClickListener(v -> {
            String kw = edtSearch.getText().toString().trim();
            if (!kw.isEmpty()) searchAndNavigate(kw);
        });

        btnStartNav.setOnClickListener(v -> startNavigation());
        btnStopNav.setOnClickListener(v -> stopNavigation());

        infoCard.setOnClickListener(v -> openPostDetail());
        txtItemName.setOnClickListener(v -> openPostDetail());
        txtItemArea.setOnClickListener(v -> openPostDetail());
        areaBoundaryHelper = new AreaBoundaryHelper();
    }

    @Override
    protected void onResume()  { super.onResume();  map.onResume(); }
    @Override
    protected void onPause()   { super.onPause();   map.onPause(); }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
        osrmHelper.shutdown();
    }
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
    //Initialization
    private void bindViews() {
        map            = findViewById(R.id.mapView);
        edtSearch      = findViewById(R.id.edtSearch);
        btnSearch      = findViewById(R.id.btnSearch);
        btnMyLocation  = findViewById(R.id.btnMyLocation);
        btnBack        = findViewById(R.id.btnBack);
        chipGroupAreas = findViewById(R.id.chipGroupAreas);

        infoCard       = findViewById(R.id.infoCard);
        txtItemName    = findViewById(R.id.txtItemName);
        txtItemArea    = findViewById(R.id.txtItemArea);
        txtRouteInfo   = findViewById(R.id.txtRouteInfo);
        btnStartNav    = findViewById(R.id.btnStartNav);

        navCard        = findViewById(R.id.navCard);
        txtNavStep     = findViewById(R.id.txtNavStep);
        txtNavDistance = findViewById(R.id.txtNavDistance);
        btnStopNav     = findViewById(R.id.btnStopNav);

        footerLayout = findViewById(R.id.footer);
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_map);

        // Ẩn footer khi bàn phím xuất hiện
        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            android.graphics.Rect r = new android.graphics.Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                if (footerLayout != null && footerLayout.getVisibility() != View.GONE) {
                    footerLayout.setVisibility(View.GONE);
                }
            } else {
                if (footerLayout != null && footerLayout.getVisibility() != View.VISIBLE) {
                    footerLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.setBuiltInZoomControls(false);
        GeoPoint nlu = new GeoPoint(10.8710, 106.7932);
        map.getController().setZoom(17.0);
        map.getController().setCenter(nlu);
    }

    private void setupSearch() {
        suggestionAdapter = new SearchSuggestionAdapter(this, allItems);
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

    private void setupAreaFilters() {
        // FIX: create proper Chip objects (not Button) so ChipGroup single-selection works
        List<String> areas = Arrays.asList(
                "All", "Dorm A", "Dorm B", "Canteen", "Library", "Hall A", "Hall B");

        chipGroupAreas.removeAllViews();

        for (String area : areas) {
            Chip chip = new Chip(this);
            chip.setText(area);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(android.R.color.transparent);

            // Mark "All" as initially checked
            if ("All".equals(area)) chip.setChecked(true);

            chipGroupAreas.addView(chip);
        }

        // Single listener on the group — no per-chip onClick needed
        chipGroupAreas.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return; // singleSelection keeps at least one checked

            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected == null) return;

            selectedArea = selected.getText().toString();

            List<ItemLocation> areaItems = getItemsForArea(selectedArea);
            suggestionAdapter.updateData(areaItems);

            if (!"All".equals(selectedArea)) {
                zoomToAreaCenter(selectedArea);
                areaBoundaryHelper.drawBoundary(map, selectedArea);
            } else {
                areaBoundaryHelper.clearBoundary(map);
                map.getController().animateTo(new GeoPoint(10.8710, 106.7932));
                map.getController().setZoom(17.0);
            }

            String kw = edtSearch.getText().toString().trim();
            if (!kw.isEmpty()) searchAndNavigate(kw);
        });
    }

    private void zoomToAreaCenter(String area) {
        GeoPoint center;

        switch (area) {
            case "Dorm A":
                center = new GeoPoint(10.8715, 106.7928);
                break;

            case "Dorm B":
                center = new GeoPoint(10.8720, 106.7940);
                break;

            case "Canteen":
                center = new GeoPoint(10.8707, 106.7925);
                break;

            case "Library":
                center = new GeoPoint(10.8723, 106.7936);
                break;

            case "Hall A":
                center = new GeoPoint(10.8730, 106.7930);
                break;

            case "Hall B":
                center = new GeoPoint(10.8736, 106.7946);
                break;

            default:
                center = new GeoPoint(10.8710, 106.7932);
                break;
        }

        map.post(() -> {
            map.getController().animateTo(center);
            map.getController().setZoom(18.5);
            map.invalidate();
        });
    }
    //Activity Navigation
    private void openPostDetail() {
        if (currentPostId == -1) {
            Toast.makeText(this, "Không tìm thấy bài đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MapActivity.this, PostDetailActivity.class);
        intent.putExtra("POST_ID", currentPostId);
        startActivity(intent);
    }

    //Search

    //Search & show results
    private void searchAndNavigate(String keyword) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        List<ItemLocation> results = new ArrayList<>();

        for (ItemLocation item : allItems) {
            boolean areaMatch = selectedArea.equals("All")
                    || item.getAreaName().equalsIgnoreCase(selectedArea);
            if (!areaMatch) continue;

            boolean matchName = item.getName().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            boolean matchDesc = item.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword);
            if (matchName || matchDesc) results.add(item);
        }

        showSearchResults(results);
    }

    private void showSearchResults(List<ItemLocation> results) {
        clearSearchMarkers();

        if (results.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
            return;
        }

        List<GeoPoint> geoPoints = new ArrayList<>();

        for (ItemLocation item : results) {
            GeoPoint point = new GeoPoint(item.getLatitude(), item.getLongitude());
            geoPoints.add(point);

            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setTitle(item.getName());
            marker.setSnippet(item.getAreaName());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Khi click marker → giữ lại markers cùng khu vực, ẩn marker khác, zoom vào
            marker.setOnMarkerClickListener((m, mapView) -> {
                filterMarkersByArea(item.getAreaName());
                focusItem(item);
                return true;
            });

            map.getOverlays().add(marker);
            searchMarkers.add(marker);
        }

        // Hiện tất cả markers trước
        if (geoPoints.size() == 1) {
            map.getController().animateTo(geoPoints.get(0));
            map.getController().setZoom(16.5);
        } else {
            BoundingBox box = BoundingBox.fromGeoPoints(geoPoints);
            map.zoomToBoundingBox(box.increaseByScale(1.5f), false);
            if (map.getZoomLevelDouble() > 17.5) {
                map.getController().setZoom(17.5);
            }
        }

        map.invalidate();
    }

    private void focusItem(ItemLocation item) {
        if (isNavigating) stopNavigation();
        currentPostId = item.getId();

        infoCard.setVisibility(View.VISIBLE);
        txtItemName.setText(item.getName());
        txtItemArea.setText(item.getAreaName() + " • " + item.getDescription());
        txtRouteInfo.setText("Đang tính đường đi...");
        btnStartNav.setVisibility(View.GONE);

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
            map.getController().setZoom(17.0);
        }
        map.invalidate();
    }

    private void filterMarkersByArea(String areaName) {
        // Lưu lại danh sách markers cùng khu vực
        List<Marker> sameAreaMarkers = new ArrayList<>();
        List<GeoPoint> sameAreaPoints = new ArrayList<>();

        for (Marker marker : searchMarkers) {
            if (areaName.equals(marker.getSnippet())) {
                // Giữ lại marker cùng khu vực
                marker.setAlpha(1.0f);
                sameAreaMarkers.add(marker);
                sameAreaPoints.add(marker.getPosition());
            } else {
                // Ẩn marker khác khu vực
                marker.setAlpha(0f);
            }
        }

        map.invalidate();

        // Zoom vào khu vực được chọn
        if (sameAreaPoints.size() == 1) {
            map.getController().animateTo(sameAreaPoints.get(0));
            map.getController().setZoom(18.5);
        } else if (sameAreaPoints.size() > 1) {
            BoundingBox box = BoundingBox.fromGeoPoints(sameAreaPoints);
            map.zoomToBoundingBox(box.increaseByScale(1.3f), true, 120);
        }
    }


    private void clearSearchResult() {
        if (isNavigating) stopNavigation();
        clearSearchMarkers();
        if (itemMarker != null) {
            map.getOverlays().remove(itemMarker);
            itemMarker = null;
        }
        clearRoute();
        infoCard.setVisibility(View.GONE);
        map.invalidate();
    }
    private void clearSearchMarkers() {
        for (Marker marker : searchMarkers) {
            marker.setAlpha(1.0f); // reset trước khi xóa
            map.getOverlays().remove(marker);
        }
        searchMarkers.clear();
    }

    //Area filter
    /** Returns items matching the currently selected area. */
    private List<ItemLocation> getItemsForArea(String area) {
        if ("All".equals(area)) return new ArrayList<>(allItems);
        List<ItemLocation> result = new ArrayList<>();
        for (ItemLocation item : allItems) {
            if (item.getAreaName().equalsIgnoreCase(area)) result.add(item);
        }
        return result;
    }


    // Route drawing
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


    // Navigation
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

        infoCard.setVisibility(View.GONE);
        navCard.setVisibility(View.VISIBLE);

        showCurrentStep();
        startLocationTracking();
    }

    private void stopNavigation() {
        isNavigating = false;
        stopLocationTracking();
        navCard.setVisibility(View.GONE);
        // Restore info card if there is a destination selected
        if (destination != null) infoCard.setVisibility(View.VISIBLE);
        if (myLocation != null && destination != null) {
            BoundingBox box = BoundingBox.fromGeoPoints(List.of(myLocation, destination));
            map.zoomToBoundingBox(box.increaseByScale(1.4f), true, 120);
        }
    }

    private void showCurrentStep() {
        if (currentStepIndex >= routeSteps.size()) return;
        OsrmRouteHelper.RouteStep step = routeSteps.get(currentStepIndex);

        String icon = directionIcon(step.maneuver);
        txtNavStep.setText(icon + "  " + step.instruction);

        // Update remaining distance to destination
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

    private String directionIcon(String maneuver) {
        if (maneuver == null) return "⬆️";
        switch (maneuver) {
            case "turn-left":         return "⬅️";
            case "turn-right":        return "➡️";
            case "turn-sharp-left":   return "↩️";
            case "turn-sharp-right":  return "↪️";
            case "turn-slight-left":  return "↖️";
            case "turn-slight-right": return "↗️";
            case "uturn":             return "🔄";
            case "arrive":            return "📍";
            default:                  return "⬆️";
        }
    }

    private void onArrived() {
        stopLocationTracking();
        isNavigating = false;
        navCard.setVisibility(View.GONE);

        // FIX: restore info card on arrival (was missing; stopNavigation() has the same logic
        // but onArrived() bypassed it)
        if (destination != null) infoCard.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Đã đến nơi!", Toast.LENGTH_LONG).show();
        if (itemMarker != null) itemMarker.showInfoWindow();
        map.invalidate();
    }

    // GPS tracking

    private void startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(2000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(1000)
                .build();

        navLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || !isNavigating) return;
                Location loc = result.getLastLocation();
                if (loc != null) onNavLocationUpdate(loc);
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

    private void onNavLocationUpdate(Location location) {
        myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Update user marker
        if (myMarker != null) map.getOverlays().remove(myMarker);
        myMarker = new Marker(map);
        myMarker.setPosition(myLocation);
        myMarker.setTitle("Bạn đang ở đây");
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(myMarker);

        if (!userInteracting) {
            map.getController().animateTo(myLocation);
            map.getController().setZoom(19.0);
        }
        map.invalidate();

        // Check arrival
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

        // FIX: advance step first, then call showCurrentStep() once
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
                }
            }
        }
        // Single call — always reflects the (possibly just advanced) step
        showCurrentStep();
    }


    //Current Location

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
                    else Toast.makeText(this,
                            "Không lấy được vị trí. Hãy bật GPS.", Toast.LENGTH_SHORT).show();
                });
    }

    private void onLocationReceived(Location location) {
        myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (myMarker != null) map.getOverlays().remove(myMarker);
        myMarker = new Marker(map);
        myMarker.setPosition(myLocation);
        myMarker.setTitle("Vị trí của bạn");
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        myMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_red));
        map.getOverlays().add(myMarker);

        if (!isNavigating) {
            map.getController().animateTo(myLocation);
            map.getController().setZoom(18.0);
        }
        map.invalidate();

        Toast.makeText(this, "Đã cập nhật vị trí!", Toast.LENGTH_SHORT).show();

        if (itemMarker != null && itemMarker.getPosition() != null) {
            drawRoute(myLocation, itemMarker.getPosition());
        }
    }
}