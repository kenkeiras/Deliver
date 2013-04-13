package com.codigoparallevar.deliver;

import org.osmdroid.util.GeoPoint;

/**
 * Representa el marcador de una tarea en el mapa.
 *
 */
public class Task{
    private int id;
    private String name;
    private boolean completed;
    private GeoPoint point;

    /**
     * Constructor.
     *
     * @param id ID de la tarea.
     * @param name Nombre de la tarea.
     * @param completed Está o no completada.
     * @param point Localización de la tarea.
     *
     */
    public Task(int id, String name, boolean completed, GeoPoint point){
        this.id = id;
        this.name = name;
        this.completed = completed;
        this.point = point;
    }

    // Getters, no hay que decir que hacen, no? ;)
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public boolean isCompleted(){
        return completed;
    }

    public GeoPoint getLocation(){
        return point;
    }
}
