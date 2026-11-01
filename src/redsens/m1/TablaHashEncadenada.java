package redsens.m1;

import redsens.modelo.Estacion;

// M1 - Desafío opcional
public class TablaHashEncadenada implements DiccionarioEstaciones {

    // con listas, λ puede pasar de 1; se usa 1,0 para que cada lista tenga en promedio un elemento
    public static final double UMBRAL = 1.0;

    private static final class NodoLista {
        final Estacion estacion;
        NodoLista siguiente;

        NodoLista(Estacion estacion, NodoLista siguiente) {
            this.estacion = estacion;
            this.siguiente = siguiente;
        }
    }

    private NodoLista[] listas;
    private int cantidad;
    private long colisiones;
    private int rehashings;
    private int sondeosUltimaOperacion;

    public TablaHashEncadenada(int tamanioInicial) {
        listas = new NodoLista[Primos.siguientePrimo(tamanioInicial)];
    }

    // la misma función que la tabla con direccionamiento abierto (Horner + mezcla de bits)
    private int hash(String clave) {
        int h = 0;
        for (int i = 0; i < clave.length(); i++) {
            h = 31 * h + clave.charAt(i);
        }
        h ^= h >>> 16;
        h *= 0x45d9f3b;
        h ^= h >>> 16;
        return Math.floorMod(h, listas.length);
    }

    @Override
    public boolean insertar(Estacion estacion) {
        if (buscar(estacion.getCodigo()) != null) {
            return false;
        }
        int pos = hash(estacion.getCodigo());
        if (listas[pos] != null) {
            colisiones++;   // la posición ya tenía al menos una estación
        }
        listas[pos] = new NodoLista(estacion, listas[pos]);
        cantidad++;
        if (cantidad > UMBRAL * listas.length) {
            rehash();
        }
        return true;
    }

    // los sondeos son los nodos comparados
    @Override
    public Estacion buscar(String codigo) {
        int comparados = 0;
        for (NodoLista n = listas[hash(codigo)]; n != null; n = n.siguiente) {
            comparados++;
            if (n.estacion.getCodigo().equals(codigo)) {
                sondeosUltimaOperacion = comparados;
                return n.estacion;
            }
        }
        sondeosUltimaOperacion = comparados;
        return null;
    }

    @Override
    public boolean eliminar(String codigo) {
        int pos = hash(codigo);
        NodoLista anterior = null;
        for (NodoLista n = listas[pos]; n != null; anterior = n, n = n.siguiente) {
            if (n.estacion.getCodigo().equals(codigo)) {
                if (anterior == null) {
                    listas[pos] = n.siguiente;
                } else {
                    anterior.siguiente = n.siguiente;
                }
                cantidad--;
                return true;
            }
        }
        return false;
    }

    private void rehash() {
        NodoLista[] anteriores = listas;
        listas = new NodoLista[Primos.siguientePrimo(2 * anteriores.length)];
        rehashings++;
        for (NodoLista cabeza : anteriores) {
            for (NodoLista n = cabeza; n != null; n = n.siguiente) {
                int pos = hash(n.estacion.getCodigo());
                listas[pos] = new NodoLista(n.estacion, listas[pos]);
            }
        }
    }

    @Override
    public int getSondeosUltimaOperacion() {
        return sondeosUltimaOperacion;
    }

    @Override
    public long getColisiones() {
        return colisiones;
    }

    @Override
    public int getRehashings() {
        return rehashings;
    }

    @Override
    public int tamanio() {
        return listas.length;
    }

    @Override
    public int cantidad() {
        return cantidad;
    }

    @Override
    public double factorCarga() {
        return (double) cantidad / listas.length;
    }

    @Override
    public String descripcion() {
        return String.format("Encadenamiento, umbral %.2f", UMBRAL);
    }
}
