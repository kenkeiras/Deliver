package com.codigoparallevar.deliver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.widget.*;

/**
 * Diálogos comunes a las distintas Activities.
 *
 */
public class CommonDialogs{
    /**
     * Elimina un marcador después de pedir la confirmación.
     *
     * @param context Contexto en el que se mostrará el diálogo.
     * @param id ID de la tarea a eliminar.
     * @param callback Callback a realizar cuando se elimina una tarea.
     *
     */
    public static void deleteElement(final Context context, final int id, final Callback callback){
        final String name = DBManager.getNameFromID(id);

        new AlertDialog.Builder(context)
            .setTitle(name)
            .setMessage(context.getString(R.string.do_you_wish_to_remove_task__before) +
                        name +
                        context.getString(R.string.do_you_wish_to_remove_task__after))
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBManager.deleteTask(id);
                        if (callback != null){
                            callback.call();
                        }
                    }
                })
            .setNegativeButton(R.string.no, null)
            .show();
    }


    /**
     * Edita el nombre de un marcador.
     *
     * @param context Contexto en el que se mostrará el diálogo.
     * @param id ID del elemento a editar.
     * @param callback Callback a realizar cuando se edite el nombre.
     *
     */
    public static void editElementName(final Context context, final int id, final Callback callback){
        final String prev_name = DBManager.getNameFromID(id);

        final AlertDialog.Builder taskNameDialog = new AlertDialog.Builder(context);
        final EditText taskNameInput = new EditText(context);
        taskNameInput.setText(prev_name);
        taskNameInput.setHint(R.string.task_to_do);
        taskNameDialog.setView(taskNameInput);
        taskNameDialog.setPositiveButton(context.getString(R.string.proceed), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Inserción de la tabla
                    DBManager.updateTaskName(id, taskNameInput.getText().toString().trim());

                    if (callback != null){
                        callback.call();
                    }
                }
            });

        taskNameDialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //
                }
            });
        taskNameDialog.show();
    }


    /**
     * Edita un elemento.
     *
     * @param context Contexto en el que se mostrará el diálogo.
     * @param id Índice del elemento a editar.
     * @param callback Callback a realizar cuando se produzca un cambio.
     *
     */
    public static void editElement(final Context context, final int id, final Callback callback){
        final String name = DBManager.getNameFromID(id);

        final CharSequence[] actions = {context.getString(R.string.toggle),
                                        context.getString(R.string.remove),
                                        context.getString(R.string.edit),
                                        context.getString(R.string.center_map)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(name);
        builder.setItems(actions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                    case 0:
                        // Cambiar estado de la tarea
                        DBManager.toggleTask(id);
                        if (callback != null){
                            callback.call();
                        }
                        break;

                    case 1:
                        // Confirmar que se quiere eliminar la tarea
                        deleteElement(context, id, callback);
                        break;

                    case 2:
                        // Preguntar el nombre de la tarea
                        editElementName(context, id, callback);
                        break;

                    case 3:
                        // Centrar el mapa en la tarea
                        Intent i = new Intent();
                        i.setClass(context, MapActivity.class);
                        i.putExtra("id", id);
                        context.startActivity(i);
                        break;
                    }
                }
            });

        builder.create().show();
    }
}
