package redsens.m5;

import java.util.Iterator;
import java.util.LinkedList;

// M5 - Solución simple de Josephus, como en la lectura
public final class JosephusSimple {

    private JosephusSimple() {
    }

    // devuelve el orden de salida
    public static int[] ordenDeSalida(int personas, int pasadas) {
        LinkedList<Integer> ronda = new LinkedList<>();
        for (int i = 1; i <= personas; i++) {
            ronda.add(i);
        }
        int[] orden = new int[personas];
        int k = 0;
        Iterator<Integer> itr = ronda.iterator();
        int actual = 0;
        while (ronda.size() > 1) {
            for (int i = 0; i <= pasadas; i++) {        // el siguiente toma la papa y la pasa M veces
                if (!itr.hasNext()) {
                    itr = ronda.iterator();             // se llegó al final: la ronda es circular
                }
                actual = itr.next();
            }
            itr.remove();                               // O(1): el iterador ya está sobre el nodo
            orden[k++] = actual;
        }
        orden[k] = ronda.getFirst();
        return orden;
    }
}
