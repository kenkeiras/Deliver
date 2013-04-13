package com.codigoparallevar.deliver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

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
    MapView mapView = null;

    // Timeout del doble click
    private static final long DOUBLE_CLICK_TIMEOUT = 250;

    // Punto en el que inicia la aplicación (Coruña)
    private static final GeoPoint INITIAL_POINT = new GeoPoint(43.365126,-8.411951);

    // Posición de la capa de Marcadores
    private static final int ITEM_OVERLAY_POS = 0;

    // Callback que actualiza el overlay cuando es llamado
    private final Callback updateTargetsOverlayCallback = new Callback(){
            public void call(){
                updateTargetsOverlay();
            }
        };

    /**
     * Genera un GeoPoint a partir de un IGeoPoint.
     *
     */
    public static GeoPoint geoPointFromIGeoPoint(IGeoPoint igp){
        return new GeoPoint(igp.getLatitudeE6(),
                            igp.getLongitudeE6());
    }


    /**
     * Añade un punto a los objetivos.
     *
     * @param gp Las coordenadas del punto a añadir.
     *
     */
    private void addPoint(final IGeoPoint gp){
        final AlertDialog.Builder taskNameDialog = new AlertDialog.Builder(this);
        final EditText taskNameInput = new EditText(this);
        taskNameInput.setText("");
        taskNameInput.setHint(R.string.task_to_do);
        taskNameDialog.setView(taskNameInput);
        taskNameDialog.setPositiveButton(getString(R.string.proceed), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    DBManager.addPoint(gp.getLatitudeE6(), gp.getLongitudeE6(),
                                       taskNameInput.getText().toString().trim());

                    // Actualizar la lista
                    updateTargetsOverlay();
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
     * Crea el overlay para los marcadores.
     *
     */
    private void setupMarkerOverlay(){
        ItemizedIconOverlay overlay = new ItemizedIconOverlay(
            context, new ArrayList<OverlayItem>(),
            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    CommonDialogs.editElement(context, DBManager.getIdFromIndex(index), updateTargetsOverlayCallback);
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    return false;
                }
            });

        assertEquals(ITEM_OVERLAY_POS, mapView.getOverlays().size());
        mapView.getOverlays().add(overlay);
    }


    /**
     * Actualiza la vista con los marcadores activados.
     *
     */
    private void updateTargetsOverlay(){
        assertTrue("Marker overlay hasn't been created.", mapView.getOverlays().size() > ITEM_OVERLAY_POS);
        Drawable cLocationMarker = context.getResources().getDrawable(R.drawable.completed_marker);
        Drawable uLocationMarker = context.getResources().getDrawable(R.drawable.uncompleted_marker);
        ItemizedIconOverlay itemOverlay = (ItemizedIconOverlay) mapView.getOverlays().get(ITEM_OVERLAY_POS);
        itemOverlay.removeAllItems();

        for (Task task: DBManager.getTasks()){

            OverlayItem overlayItem = new OverlayItem(task.getName(), getString(task.isCompleted() ?
                                                                             R.string.completed_task :
                                                                             R.string.uncompleted_task),
                                                      task.getLocation());

            overlayItem.setMarker(task.isCompleted() ? cLocationMarker: uLocationMarker);
            itemOverlay.addItem(overlayItem);
        }

        // Forzar actualización del canvas
        mapView.getController().scrollBy(1, 1);
        mapView.getController().scrollBy(-1, -1);
    }


    /**
     * Prepara el MapView para la acción.
     *
     */
    private void setupMapView(){
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13);
        mapView.getController().setCenter(INITIAL_POINT);

        setupMarkerOverlay();

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
                    }
                    // Si hace doble click deja un marcador
                    else if (event.getAction() == MotionEvent.ACTION_DOWN){
                        if ((time - lastTouchTime) < DOUBLE_CLICK_TIMEOUT){
                            lastTouchTime = -1;

                            IGeoPoint geoPoint = fMapView.getProjection().fromPixels(X, Y);
                            addPoint(geoPoint);
                            return true;
                        }
                        else{
                            lastTouchTime = time;
                        }

                    }
                    // Si retira rápido el dedo suponemos que está señalando un objeto
                    else if (event.getAction() == MotionEvent.ACTION_UP){
                        if ((time - lastTouchTime) < DOUBLE_CLICK_TIMEOUT){
                            return mapView.getOverlays().get(ITEM_OVERLAY_POS).onSingleTapUp(event, mapView);
                        }
                    }
                    lastX = X;
                    lastY = Y;
                    return false;
                }
            });
        updateTargetsOverlay();
    }


    /**
     * Despliegue del menú.
     *
     * @param menu
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }


    /**
     * Descripción: Maneja la acción de seleccionar un item del menú.
     *
     * @param item Item seleccionado.
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        // Handle item selection
        switch (item.getItemId()) {
        case R.id.show_element_list_icon:
            i = new Intent();
            i.setClass(this, PlainListActivity.class);
            startActivity(i);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }


    /** Al eliminar cerrar la base de datos. */
    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    /** Al volver de la lista, se comprueban los cambios. */
    @Override
    public void onResume(){
        super.onResume();
        DBManager.initialize(context);
        updateTargetsOverlay();
    }


    /**
     * Centra la vista en una tarea.
     *
     * @param id ID de la tarea a centrar.
     *
     */
    public void centerOnTask(int id){
        mapView.getController().setCenter(DBManager.getTask(id).getLocation());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.map);
        mapView = (MapView) findViewById(R.id.mapView);

        DBManager.initialize(context);
        setupMapView();

        int centerTaskId = getIntent().getIntExtra("id", -1);
        if (centerTaskId != -1){
            centerOnTask(centerTaskId);
        }
    }

}
