package com.example.edushare.feature.locations.helper;

import android.graphics.Color;
import android.graphics.DashPathEffect;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AreaBoundaryHelper {

    private Polygon currentBoundary;

    public void drawBoundary(MapView map, String areaName) {
        clearBoundary(map);

        List<GeoPoint> points = getBoundaryPoints(areaName);
        if (points.isEmpty()) return;

        currentBoundary = new Polygon();
        currentBoundary.setPoints(points);

        currentBoundary.getOutlinePaint().setColor(Color.parseColor("#EF4444"));
        currentBoundary.getOutlinePaint().setStrokeWidth(8f);
        currentBoundary.getOutlinePaint().setAlpha(230);

        // Nét đứt
        currentBoundary.getOutlinePaint().setPathEffect(
                new DashPathEffect(new float[]{30, 18}, 0)
        );

        // Tô nền nhạt
        currentBoundary.getFillPaint().setColor(Color.parseColor("#22EF4444"));

        map.getOverlays().add(currentBoundary);
        map.invalidate();
    }

    public void clearBoundary(MapView map) {
        if (currentBoundary != null) {
            map.getOverlays().remove(currentBoundary);
            currentBoundary = null;
            map.invalidate();
        }
    }

    private List<GeoPoint> getBoundaryPoints(String areaName) {
        switch (areaName) {
            case "Dorm A":
                return Arrays.asList(
                        new GeoPoint(10.87225, 106.79205),
                        new GeoPoint(10.87220, 106.79330),
                        new GeoPoint(10.87155, 106.79355),
                        new GeoPoint(10.87095, 106.79320),
                        new GeoPoint(10.87085, 106.79225),
                        new GeoPoint(10.87145, 106.79195)
                );

            case "Dorm B":
                return Arrays.asList(
                        new GeoPoint(10.87275, 106.79325),
                        new GeoPoint(10.87270, 106.79475),
                        new GeoPoint(10.87205, 106.79505),
                        new GeoPoint(10.87135, 106.79465),
                        new GeoPoint(10.87125, 106.79365),
                        new GeoPoint(10.87190, 106.79315)
                );

            case "Canteen":
                return Arrays.asList(
                        new GeoPoint(10.87115, 106.79185),
                        new GeoPoint(10.87105, 106.79280),
                        new GeoPoint(10.87035, 106.79295),
                        new GeoPoint(10.87005, 106.79235),
                        new GeoPoint(10.87030, 106.79170)
                );

            case "Library":
                return Arrays.asList(
                        new GeoPoint(10.87290, 106.79305),
                        new GeoPoint(10.87295, 106.79410),
                        new GeoPoint(10.87235, 106.79435),
                        new GeoPoint(10.87175, 106.79385),
                        new GeoPoint(10.87190, 106.79305)
                );

            case "Hall A":
                return Arrays.asList(
                        new GeoPoint(10.87355, 106.79245),
                        new GeoPoint(10.87365, 106.79345),
                        new GeoPoint(10.87305, 106.79385),
                        new GeoPoint(10.87245, 106.79325),
                        new GeoPoint(10.87260, 106.79245)
                );

            case "Hall B":
                return Arrays.asList(
                        new GeoPoint(10.87405, 106.79385),
                        new GeoPoint(10.87395, 106.79515),
                        new GeoPoint(10.87325, 106.79535),
                        new GeoPoint(10.87285, 106.79455),
                        new GeoPoint(10.87315, 106.79375)
                );

            default:
                return new ArrayList<>();
        }
    }
}