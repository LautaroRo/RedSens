package redsens.m5;

// M5 - Alternativa eficiente de Josephus
public final class JosephusArbol {

    private JosephusArbol() {
    }

    public static int[] ordenDeSalida(int personas, int pasadas) {
        ArbolRango ronda = new ArbolRango(personas);
        int[] orden = new int[personas];
        int k = 0;
        int rango = 1;          // posición de quien tiene la papa
        int quedan = personas;
        while (quedan > 1) {
            // Desde la posición actual se avanzan M lugares, dando la vuelta si hace falta.
            // Al eliminar, el siguiente pasa a ocupar esa misma posición, por eso no se suma 1.
            rango = (rango + pasadas) % quedan;
            if (rango == 0) {
                rango = quedan;
            }
            orden[k++] = ronda.eliminarKesimo(rango);
            quedan--;
        }
        orden[k] = ronda.kesimo(1);
        return orden;
    }
}
