package com.codigoparallevar.deliver;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.database.sqlite.*;
import android.database.Cursor;


import java.util.ArrayList;
import java.util.List;
import org.osmdroid.util.GeoPoint;

/**
 * Maneja las interacciones con la base de datos.
 *
 */
public class DBManager{
    private static SQLiteDatabase sqldb = null;
    private static String DB_NAME = "DELIVER_DB";
    private static String[] sqlcols = new String[]{"_id", "LATITUDE", "LONGITUDE", "TASK", "COMPLETED"};


    /** Clase estática */
    private DBManager(){}


    /**
     * Añade un punto a los objetivos.
     *
     * @param latitude Latitud del punto del objetivo* 10^6.
     * @param longitude Longitud del punto del objetivo* 10^6.
     * @param taskName Nombre de la tarea.
     *
     */
    public static void addPoint(int latitude, int longitude, String taskName){
        ContentValues cv = new ContentValues(4);
        cv.put("LATITUDE", latitude);
        cv.put("LONGITUDE", longitude);
        cv.put("TASK", taskName);
        cv.put("COMPLETED", false);

        sqldb.insert(DB_NAME, null, cv);
    }


    /**
     * Cambia el estado de una tarea entre completo e incompleto.
     *
     * @param id ID de la tarea.
     *
     */
    public static void toggleTask(int id){
        // Teóricamente habrá que usar sqldb.update, pero así es bastante más directo.
        sqldb.execSQL("UPDATE " + DB_NAME +" SET " +
                      "COMPLETED = NOT COMPLETED " +
                      "WHERE _id = " + id);
    }


    /**
     * Cambia el nombre de una tarea.
     *
     * @param id ID de la tarea.
     * @param name Nuevo nombre de la tarea.
     *
     */
    public static void updateTaskName(int id, String name){
        ContentValues cv = new ContentValues(1);
        cv.put("TASK", name);

        sqldb.update(DB_NAME, cv, "_id = ?", new String[]{id + ""});
    }


    /**
     * Elimina una tarea.
     *
     * @param id ID de la tarea.
     *
     */
    public static void deleteTask(int id){
        sqldb.delete(DB_NAME, "_id = ?", new String[]{id + ""});
    }


    /**
     * Cambia el nombre de una tarea.
     *
     * @param id ID de la tarea.
     * @param name El nuevo nombre de la tarea.
     *
     */
    public static void toogleTask(int id, String name){
        ContentValues cv = new ContentValues(1);
        cv.put("TASK", name);

        sqldb.update(DB_NAME, cv, "_id = ?", new String[]{id + ""});
    }


    /**
     * A partir del índice de búsqueda devuelve el ID de un elemento tomado de `getTasks'.
     *
     * @param index Número que ocupa el documento en la query.
     *
     * @see #getTasks()
     */
    public static int getIdFromIndex(int index){
       Cursor c = sqldb.query(DB_NAME, sqlcols,
                               null, null, null, null, null, index + ", 1");

       c.moveToFirst();
       final int id = c.getInt(c.getColumnIndex("_id"));
       c.close();

       return id;
    }


    /**
     * Devuelve el nombre de una tarea a partir del ID.
     *
     * @param id ID de la tarea.
     *
     */
    public static String getNameFromID(int id){
       Cursor c = sqldb.query(DB_NAME, sqlcols,
                              "_id = ?", new String[]{id + ""},
                              null, null, null);

       c.moveToFirst();
       final String name = c.getString(c.getColumnIndex("TASK"));
       c.close();

       return name;
    }


    /**
     * Constuye un objeto Task en base a la posición actual del cursor.
     *
     * @param c Cursor en base del que se construirá la tarea.
     *
     */
    private static Task buildTaskFromCursor(Cursor c){
        boolean completed = c.getInt(4) != 0;

        return new Task(c.getInt(0),
                        c.getString(3), completed,
                        new GeoPoint((int) c.getLong(1), (int) c.getLong(2)));
    }


    /**
     * Devuelve los items marcados.
     *  Se puede recuperar el ID a partir del índice con `getIdFromIndex'.
     *
     * @see #getIdFromIndex(int id)
     */
    public static List<Task> getTasks(){


        Cursor c = sqldb.query(DB_NAME, sqlcols, null, null, null, null, null, null);
        List<Task> taskList = new ArrayList<Task>(c.getCount());

        if (c.moveToFirst()){
            do{
                taskList.add(buildTaskFromCursor(c));
            }while (c.moveToNext());
        }
        c.close();

        return taskList;
    }


    /**
     * Devuelve una tarea en base a su ID.
     *
     * @param id ID del punto.
     *
     */
    public static Task getTask(int id){
        Cursor c = sqldb.query(DB_NAME, sqlcols, "_id = ?", new String[]{id + ""}, null, null, null, null);

        c.moveToFirst();
        Task task = buildTaskFromCursor(c);
        c.close();

        return task;
    }


    /**
     * Cerrar la conexión.
     *
     *  Puede que dé problemas, no es recomendable hacerlo a menos que sea realmente necesario.
     *
     */
    public static void close(){
        if ((sqldb != null) && (sqldb.isOpen())){
            sqldb.close();
        }
        sqldb = null;
    }


    /**
     * Inicializar todo lo necesario (en caso de serlo).
     *
     * @param context Contexto donde se ejecutará.
     *
     */
    public static void initialize(Context context){
        if ((sqldb != null) && (sqldb.isOpen())){
            sqldb.close();
        }

        sqldb = context.openOrCreateDatabase(DB_NAME, context.MODE_PRIVATE, null);

        // Crea la tabla si no existe
        sqldb.execSQL("CREATE TABLE IF NOT EXISTS " + DB_NAME +
                      " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                      " LATITUDE LONG, LONGITUDE LONG, " +
                      " TASK VARCHAR, COMPLETED BOOLEAN);");
    }

}
