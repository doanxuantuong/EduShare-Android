package com.example.edushare.feature.locations.helper;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gọi OSRM để lấy route + steps chỉ dẫn từng bước.
 * Server: routing.openstreetmap.de (đi bộ)
 * Fallback: router.project-osrm.org (lái xe)
 */
public class OsrmRouteHelper {

    private static final String TAG = "OsrmRouteHelper";

    private static final String SERVER_PRIMARY =
            "https://routing.openstreetmap.de/routed-foot/route/v1/foot/%s,%s;%s,%s"
                    + "?overview=full&geometries=polyline&steps=true&annotations=false";

    private static final String SERVER_FALLBACK =
            "https://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s"
                    + "?overview=full&geometries=polyline&steps=true&annotations=false";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── Model cho 1 bước chỉ dẫn ──────────────────────────────────────────
    public static class RouteStep {
        public String instruction; // "Rẽ trái vào Đường ABC"
        public String maneuver;    // "turn-left", "turn-right", "straight",...
        public double distance;    // mét
        public GeoPoint location;  // tọa độ bắt đầu bước này

        public RouteStep(String instruction, String maneuver,
                         double distance, GeoPoint location) {
            this.instruction = instruction;
            this.maneuver    = maneuver;
            this.distance    = distance;
            this.location    = location;
        }
    }

    public interface RouteCallback {
        void onRouteReady(List<GeoPoint> points,
                          List<RouteStep> steps,
                          double distanceMeters,
                          double durationSeconds);
        void onError(String message);
    }

    public void fetchRoute(GeoPoint from, GeoPoint to, RouteCallback callback) {
        executor.execute(() -> {
            String err = tryFetch(SERVER_PRIMARY, from, to, callback);
            if (err != null) {
                Log.w(TAG, "Primary failed: " + err + " → trying fallback");
                tryFetch(SERVER_FALLBACK, from, to, callback);
            }
        });
    }

    // return null nếu OK, error string nếu thất bại
    private String tryFetch(String template, GeoPoint from, GeoPoint to,
                            RouteCallback callback) {
        try {
            String urlStr = String.format(template,
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(),   to.getLatitude());

            Log.d(TAG, "Request: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "EduShareApp/1.0");

            if (conn.getResponseCode() != 200) {
                return "HTTP " + conn.getResponseCode();
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            if (!"Ok".equals(json.optString("code"))) {
                return "code=" + json.optString("code");
            }

            JSONObject route    = json.getJSONArray("routes").getJSONObject(0);
            double distance     = route.getDouble("distance");
            double duration     = route.getDouble("duration");
            String geometry     = route.getString("geometry");

            List<GeoPoint> points = decodePolyline(geometry);
            List<RouteStep> steps = parseSteps(route);

            mainHandler.post(() -> callback.onRouteReady(points, steps, distance, duration));
            return null;

        } catch (Exception e) {
            Log.e(TAG, "fetchRoute error", e);
            return e.getMessage();
        }
    }

    // Parse steps từ OSRM response → list RouteStep
    private List<RouteStep> parseSteps(JSONObject route) {
        List<RouteStep> steps = new ArrayList<>();
        try {
            JSONArray legs = route.getJSONArray("legs");
            for (int l = 0; l < legs.length(); l++) {
                JSONArray stepsArr = legs.getJSONObject(l).getJSONArray("steps");
                for (int s = 0; s < stepsArr.length(); s++) {
                    JSONObject step = stepsArr.getJSONObject(s);

                    double dist = step.optDouble("distance", 0);

                    // Lấy maneuver type
                    String maneuver = "straight";
                    GeoPoint location = null;
                    if (step.has("maneuver")) {
                        JSONObject man = step.getJSONObject("maneuver");
                        maneuver = man.optString("type", "straight")
                                + (man.has("modifier")
                                ? "-" + man.getString("modifier").replace(" ", "-")
                                : "");
                        if (man.has("location")) {
                            JSONArray loc = man.getJSONArray("location");
                            location = new GeoPoint(loc.getDouble(1), loc.getDouble(0));
                        }
                    }

                    // Tạo instruction dễ đọc
                    String name = step.optString("name", "");
                    String instruction = buildInstruction(maneuver, name, dist);

                    steps.add(new RouteStep(instruction, maneuver, dist, location));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseSteps error", e);
        }
        return steps;
    }

    // Tạo câu chỉ dẫn tiếng Việt từ maneuver + tên đường + khoảng cách
    private String buildInstruction(String maneuver, String roadName, double dist) {
        String distStr = dist >= 1000
                ? String.format("%.1f km", dist / 1000)
                : String.format("%.0f m", dist);

        String road = (roadName != null && !roadName.isEmpty()) ? " vào " + roadName : "";

        if (maneuver.contains("arrive")) return "Đã đến nơi" + road;
        if (maneuver.contains("left"))   return "Rẽ trái" + road + " sau " + distStr;
        if (maneuver.contains("right"))  return "Rẽ phải" + road + " sau " + distStr;
        if (maneuver.contains("uturn"))  return "Quay đầu sau " + distStr;
        if (maneuver.contains("roundabout")) return "Vào vòng xoay" + road;
        return "Đi thẳng " + distStr + road;
    }

    // Decode Google Encoded Polyline (precision 1e-5)
    private List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; }
            while (b >= 0x20);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            shift = 0; result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; }
            while (b >= 0x20);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            points.add(new GeoPoint(lat / 1e5, lng / 1e5));
        }
        return points;
    }

    public void shutdown() { executor.shutdown(); }
}