Deliver
=======

### Concepto

Como apoyo al proceso de distibución de productos y establecimiento de
contactos se ofrece una aplicación que permita manejar puntos a visitar
junto con las tareas que hay que realizar en ellos, así como si se
han completado o no.


### Generación

Para generar la documentación solo hay que ejecutar el script 'gen\_javadoc.sh' (en entornos *nix) o
'gen_javadoc.bat' (en entornos NT, este no ha sido probado).

Para la generación del instalador se ejecutará 'ant debug', o 'ant debug install' para instalarlo en el proceso.


### Librerías

Además de las librerías de android se ha utilizado [osmdroid](https://code.google.com/p/osmdroid/) (Licencia Apache 2.0/CC3), para dibujar los mapas en base a OpenStreetMap.

### Manejo

Es posible desplazarse por el mapa arrastrandolo, y hacer zoom de la forma habitual. Para añadur una tarea hay que hacer doble-click sobre el punto donde se añadirá.
Se puede navegar entre el mapa ya la lista usando el menú, en el mapa los puntos se puede editar pulsando sobre ellos, en la lista habrá que mantener pulsado el elemento para hacerlo.
