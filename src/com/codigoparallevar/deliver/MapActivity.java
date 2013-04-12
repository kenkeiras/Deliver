package com.codigoparallevar.deliver;

import android.app.Activity;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;

/**
 * Implementa un mapa que indica las posiciones apoyandose en OpenStreetMap.
 *
 */
public class MapActivity extends Activity{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        MapView mapView = new MapView(this, 256);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);

        mapView.getController().setZoom(13);
        mapView.getController().setCenter(new GeoPoint(43.365126,-8.411951));

        setContentView(mapView);
    }
}
