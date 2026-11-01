package redsens.m3;

// representación con lista de adyacencia
public class GrafoLista implements Grafo {

    private static final class Arista {
        final int destino;
        final int km;
        Arista siguiente;

        Arista(int destino, int km, Arista siguiente) {
            this.destino = destino;
            this.km = km;
            this.siguiente = siguiente;
        }
    }

    private final String[] nombres;
    private final Arista[] listas;
    private final int[] grado;
    private int aristas;

    public GrafoLista(String[] nombres) {
        this.nombres = nombres.clone();
        listas = new Arista[nombres.length];
        grado = new int[nombres.length];
    }

    @Override
    public int cantidadVertices() {
        return nombres.length;
    }

    @Override
    public int cantidadAristas() {
        return aristas;
    }

    @Override
    public String nombre(int v) {
        return nombres[v];
    }

    @Override
    public int indiceDe(String nombre) {
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equalsIgnoreCase(nombre.trim())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void agregarArista(int u, int v, int km) {
        if (peso(u, v) == INFINITO) {
            aristas++;
        }
        insertarOrdenado(u, v, km);
        insertarOrdenado(v, u, km);
    }

    // cada lista se mantiene ordenada por índice de destino, para que BFS y DFS visiten los vecinos en el mismo orden que con la matriz y las dos representaciones den exactamente el mismo resultado
    private void insertarOrdenado(int u, int v, int km) {
        if (listas[u] == null || listas[u].destino >= v) {
            if (listas[u] != null && listas[u].destino == v) {
                listas[u] = new Arista(v, km, listas[u].siguiente);
                return;
            }
            listas[u] = new Arista(v, km, listas[u]);
            grado[u]++;
            return;
        }
        Arista a = listas[u];
        while (a.siguiente != null && a.siguiente.destino < v) {
            a = a.siguiente;
        }
        if (a.siguiente != null && a.siguiente.destino == v) {
            a.siguiente = new Arista(v, km, a.siguiente.siguiente);
            return;
        }
        a.siguiente = new Arista(v, km, a.siguiente);
        grado[u]++;
    }

    @Override
    public boolean eliminarArista(int u, int v) {
        if (!quitar(u, v)) {
            return false;
        }
        quitar(v, u);
        aristas--;
        return true;
    }

    private boolean quitar(int u, int v) {
        Arista anterior = null;
        for (Arista a = listas[u]; a != null; anterior = a, a = a.siguiente) {
            if (a.destino == v) {
                if (anterior == null) {
                    listas[u] = a.siguiente;
                } else {
                    anterior.siguiente = a.siguiente;
                }
                grado[u]--;
                return true;
            }
        }
        return false;
    }

    @Override
    public int peso(int u, int v) {
        if (u == v) {
            return 0;
        }
        for (Arista a = listas[u]; a != null; a = a.siguiente) {
            if (a.destino == v) {
                return a.km;
            }
        }
        return INFINITO;
    }

    @Override
    public int[] vecinos(int v) {
        int[] resultado = new int[grado[v]];
        int i = 0;
        for (Arista a = listas[v]; a != null; a = a.siguiente) {
            resultado[i++] = a.destino;
        }
        return resultado;
    }

    @Override
    public String nombreRepresentacion() {
        return "lista de adyacencia";
    }

    @Override
    public String mostrar() {
        StringBuilder sb = new StringBuilder("Lista de adyacencia\n");
        for (int u = 0; u < nombres.length; u++) {
            sb.append(String.format("  v%-2d %-17s ->", u, nombres[u]));
            for (Arista a = listas[u]; a != null; a = a.siguiente) {
                sb.append(String.format(" %s (%d)", nombres[a.destino], a.km));
                if (a.siguiente != null) {
                    sb.append(',');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
