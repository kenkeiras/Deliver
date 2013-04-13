package com.codigoparallevar.deliver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;


import java.util.List;


/**
 * Muestra las tareas en una lista.
 *
 */
public class PlainListActivity extends Activity{
    Context context = null;


    /**
     * Manaja la creación de la lista.
     *
     */
    private class PlainListAdapter extends ArrayAdapter<Task> {
        List<Task> items;

        public PlainListAdapter(Context context, int textViewResourceId, List<Task> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }


        /**
         * Llamado por el ListView, genera la vista para un elemento e una posición.
         *
         * @param position Posición del elemento a generar.
         * @param convertView Si existe (no es null), se creará sobre esta vista.
         * @param parent Vista que contiene a convertView (no se usa).
         *
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater layout = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layout.inflate(R.layout.task_list_item, null);
            }

            CheckBox checkBox = (CheckBox) v.findViewById(R.id.task_completed);
            TextView taskName = (TextView) v.findViewById(R.id.task_name);

            final Task task = items.get(position);
            if (task != null){
                v.setClickable(true);
                if (checkBox != null){
                    checkBox.setChecked(task.isCompleted());
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            DBManager.toggleTask(task.getId());
                        }
                    });
                }

                if (taskName != null){
                    taskName.setText(task.getName());
                }
            }
            return v;
        }
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
        List<Task> taskList = DBManager.getTasks();

        // Si no se encuentran datos se vuelve al mapa.
        if (taskList.size() == 0){
            Intent i = new Intent();
            i.setClass(this, MapActivity.class);
            startActivity(i);
        }
        // Datos encontrados
        else{
            ListView listView = (ListView) findViewById(R.id.task_list);

            PlainListAdapter adapter = new PlainListAdapter(this, R.layout.plain_list, taskList);
            listView.setAdapter(adapter);
        }
    }


    /**
     * Actualiza la lista al retomar el control.
     */
    @Override
    public void onResume(){
        super.onResume();

        setContentView(R.layout.plain_list);
        DBManager.initialize(context);

        // Prepara el manejador para los clicks en la lista
        ListView listView = (ListView) findViewById(R.id.task_list);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    longTouchCallback(position);
                    return true;
                }
            });
        this.refreshTaskList();
    }


    /** Al eliminar cerrar la base de datos. */
    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
    }
}
