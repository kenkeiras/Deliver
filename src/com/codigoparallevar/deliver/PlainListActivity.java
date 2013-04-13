package com.codigoparallevar.deliver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.*;

import android.database.sqlite.*;
import android.database.Cursor;


/**
 * Muestra las tareas en una lista.
 *
 */
public class PlainListActivity extends Activity{
    /**
     * Manaja la creación de la lista.
     *
     */
    class PlainListViewBinder implements SimpleCursorAdapter.ViewBinder {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex){
            // Setup checkbox
            if (columnIndex == cursor.getColumnIndex("COMPLETED")){
                final CheckBox cb = (CheckBox) view;
                final int id = cursor.getInt(cursor.getColumnIndex("_id"));
                cb.setChecked(cursor.getInt(columnIndex) != 0);
                cb.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            sqldb.execSQL("update " + DB_NAME + " set "+
                                          "COMPLETED = " + (cb.isChecked() ? 1 : 0) + " " +
                                          "WHERE _id = " + id);
                        }
                    });
                return true;
            }
            else {
                return false;
            }
        }
    }


    static SQLiteDatabase sqldb;
    public static String DB_NAME = "DELIVER_DB";
    static String[] sqlcols = new String[]{"_id", "LATITUDE", "LONGITUDE", "TASK", "COMPLETED"};


    /**
     * Despliegue del menú.
     *
     * @param menu
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.plain_list_menu, menu);
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
        case R.id.show_map_icon:
            i = new Intent();
            i.setClass(this, MapActivity.class);
            startActivity(i);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Maneja una pulsación prolongada sobre un elemento de la lista.
     *
     * @param position Posición del elemento en cuestión.
     * @note STUB
     */
    public void longTouchCallback(int position){
    }


    /**
     * Actualiza la vista de la lista.
     *  En caso de no haber datos se vuelve al mapa.
     *
     */
    public void refreshTaskList(){
        String[] uitables = new String[]{"COMPLETED", "TASK"};
        int[] fields = new int[]{R.id.task_completed,
                                 R.id.task_name};

        Cursor c = sqldb.query(DB_NAME, this.sqlcols,
                               null, null, null, null, null, null);

        startManagingCursor(c);
        int rows = c.getCount();

        // Si no se encuentran datos se vuelve al mapa.
        if (rows == 0){
            Intent i = new Intent();
            i.setClass(this, MapActivity.class);
            sqldb.close();
            startActivity(i);
        }
        // Datos encontrados
        else{
            ListView listView = (ListView) findViewById(R.id.task_list);

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.task_list_item,
                c,
                uitables,
                fields
            );

            adapter.setViewBinder(new PlainListViewBinder());
            listView.setAdapter(adapter);

            int i;
            for (i = 1; i < (rows - 1); i++){
                if (listView.getChildAt(i) != null){
                    listView.getChildAt(i).setClickable(true);
                }
            }

        }
    }


    /**
     * Actualiza la lista al retomar el control.
     */
    @Override
    public void onResume(){
        super.onResume();
        sqldb = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        this.refreshTaskList();
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
        setContentView(R.layout.plain_list);
        sqldb = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);

        // Prepara el manejador para los clicks en la lista
        ListView listView = (ListView) findViewById(R.id.task_list);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    longTouchCallback(position);
                    return true;
                }
            });
    }
}
