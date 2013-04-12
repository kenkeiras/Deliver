package com.codigoparallevar.deliver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.os.Bundle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;

import android.database.sqlite.*;
import android.database.Cursor;


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

    static SQLiteDatabase sqldb;
    public static String DB_NAME = "DELIVER_DB";
    static String[] sqlcols = new String[]{"_id", "LATITUDE", "LONGITUDE", "TASK", "COMPLETED"};

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
     * Prepara la base de datos.
     *
     */
    private void setupDB(){
        sqldb = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);

        // Crea la tabla si no existe
        sqldb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_NAME +
                      " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                      " LATITUDE LONG, LONGITUDE LONG, " +
                      " TASK VARCHAR, COMPLETED BOOLEAN);");

    }


    /**
     * Añade un punto a los objetivos.
     *
     * @param gp Las coordenadas del punto a añadir.
     * @param mapView El mapa a actualizar si se añade.
     *
     */
    private void addPoint(final IGeoPoint gp, final MapView mapView){
        final AlertDialog.Builder taskNameDialog = new AlertDialog.Builder(this);
        final EditText taskNameInput = new EditText(this);
        taskNameInput.setText("");
        taskNameInput.setHint(R.string.task_to_do);
        taskNameDialog.setView(taskNameInput);
        taskNameDialog.setPositiveButton(getString(R.string.proceed), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Inserción de la tabla
                    ContentValues cv = new ContentValues(2);
                    cv.put("LATITUDE", gp.getLatitudeE6());
                    cv.put("LONGITUDE", gp.getLongitudeE6());
                    cv.put("TASK", taskNameInput.getText().toString().trim());
                    cv.put("COMPLETED", false);

                    sqldb.insert(DB_NAME, null, cv);

                    // Actualizar la lista
                    updateTargetsOverlay(mapView);

                    // Forzar actualización del canvas
                    mapView.getController().scrollBy(1, 1);
                    mapView.getController().scrollBy(-1, -1);
                }
            });

        taskNameDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //
                }
            });
        taskNameDialog.show();

    }


    /**
     * Actualiza la vista con los marcadores activados.
     *
     * @param mapView La vista del mapa a actualizar.
     *
     */
    private void updateTargetsOverlay(MapView mapView){
        Drawable cLocationMarker = context.getResources().getDrawable(R.drawable.completed_marker);
        Drawable uLocationMarker = context.getResources().getDrawable(R.drawable.uncompleted_marker);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        Cursor c = sqldb.query(DB_NAME, sqlcols, null, null, null, null, null, null);
        if (c.moveToFirst()){
            do{
                boolean completed = c.getInt(4) != 0;

                OverlayItem overlayItem = new OverlayItem(c.getString(3), getString(completed ?
                                                                                    R.string.completed_task :
                                                                                    R.string.uncompleted_task),
                                                          new GeoPoint((int) c.getLong(1), (int) c.getLong(2)));

                overlayItem.setMarker(completed ? cLocationMarker: uLocationMarker);
                items.add(overlayItem);
            }while (c.moveToNext());
        }
        c.close();

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

        mapView.getOverlays().add(currentLocationOverlay);
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
                            addPoint(geoPoint, fMapView);
                            return true;
                        }
                        else{
                            lastTouchTime = time;
                        }

                    }
                    return false;
                }
            });
        updateTargetsOverlay(mapView);
    }


    /** Al eliminar cerrar la base de datos. */
    @Override
    public void onDestroy(){
        super.onDestroy();
        sqldb.close();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        setContentView(R.layout.map);

        setupDB();
        setupMapView();
    }

}
