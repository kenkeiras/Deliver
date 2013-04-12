package com.codigoparallevar.deliver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;


import java.util.ArrayList;
import java.util.List;


import org.osmdroid.api.IGeoPoint;

import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.LayoutParams;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.util.GeoPoint;




/**
 * Implementa un mapa que indica las posiciones apoyandose en OpenStreetMap.
 *
 */
public class MapActivity extends Activity{

    Context context = null;
    long lastTouchTime = -1;
    int lastX = 0;
    int lastY = 0;

    // Timeout del doble click
    private static final long DOUBLE_CLICK_TIMEOUT = 250;

    // Punto en el que inicia la aplicación (Coruña)
    private static final GeoPoint INITIAL_POINT = new GeoPoint(43.365126,-8.411951);

    /**
     * Genera un GeoPoint a partir de un IGeoPoint.
     *
     */
    public static GeoPoint geoPointFromIGeoPoint(IGeoPoint igp){
        return new GeoPoint(igp.getLatitudeE6(),
                            igp.getLongitudeE6());
    }


    /**
     * Prepara el MapView para la acción.
     *
     */
    private void setupMapView(){
        MapView mapView = (MapView) findViewById(R.id.mapView);

        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13);
        mapView.getController().setCenter(INITIAL_POINT);

        // Manejo de las interacciones con el mapa
        final MapView fMapView = mapView;
        mapView.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int X = (int)event.getX();
                    int Y = (int)event.getY();

                    long time = System.currentTimeMillis();

                    Log.d("Deliver", "" + (time - lastTouchTime));

                    // Maneja el mapa arrastrando
                    if (event.getAction() == MotionEvent.ACTION_MOVE){
                        fMapView.getController().scrollBy(lastX - X, lastY - Y);

                        lastX = X;
                        lastY = Y;
                    }
                    // Si hace doble click deja un marcador
                    else if (event.getAction() == MotionEvent.ACTION_DOWN){
                        lastX = X;
                        lastY = Y;
                        if ((time - lastTouchTime) < DOUBLE_CLICK_TIMEOUT){
                            lastTouchTime = -1;

                            IGeoPoint geoPoint = fMapView.getProjection().fromPixels(X, Y);

                            OverlayItem overlayItem = new OverlayItem("Here", "Current Position",
                                                                      geoPointFromIGeoPoint(geoPoint));

                            Drawable locationMarker = context.getResources().getDrawable(R.drawable.marker);
                            overlayItem.setMarker(locationMarker);

                            final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                            items.add(overlayItem);

                            ItemizedIconOverlay currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
                                context, items,
                                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                        return true;
                                    }
                                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                                        return true;
                                    }
                                });

                            fMapView.getOverlays().add(currentLocationOverlay);

                            // Forzar actualización
                            fMapView.getController().scrollBy(1, 1);
                            fMapView.getController().scrollBy(-1, -1);
                            return true;
                        }
                        else{
                            lastTouchTime = time;
                        }

                    }
                    return false;
                }
            });
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.getResources().getDrawable(R.drawable.marker);

        context = getApplicationContext();
        setContentView(R.layout.map);
        setupMapView();
    }

}
