package redsens.m3;

// representación con matriz de adyacencia
public class GrafoMatriz implements Grafo {

    private final String[] nombres;
    private final int[][] matriz;
    private int aristas;

    public GrafoMatriz(String[] nombres) {
        this.nombres = nombres.clone();
        int n = nombres.length;
        matriz = new int[n][n];
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                matriz[u][v] = u == v ? 0 : INFINITO;
            }
        }
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
        if (matriz[u][v] == INFINITO) {
            aristas++;
        }
        matriz[u][v] = km;      // no dirigido: se carga en los dos sentidos
        matriz[v][u] = km;
    }

    @Override
    public boolean eliminarArista(int u, int v) {
        if (u == v || matriz[u][v] == INFINITO) {
            return false;
        }
        matriz[u][v] = INFINITO;
        matriz[v][u] = INFINITO;
        aristas--;
        return true;
    }

    @Override
    public int peso(int u, int v) {
        return matriz[u][v];
    }

    @Override
    public int[] vecinos(int v) {
        int cantidad = 0;
        for (int w = 0; w < nombres.length; w++) {
            if (w != v && matriz[v][w] != INFINITO) {
                cantidad++;
            }
        }
        int[] resultado = new int[cantidad];
        int i = 0;
        for (int w = 0; w < nombres.length; w++) {
            if (w != v && matriz[v][w] != INFINITO) {
                resultado[i++] = w;
            }
        }
        return resultado;
    }

    @Override
    public String nombreRepresentacion() {
        return "matriz de adyacencia";
    }

    @Override
    public String mostrar() {
        StringBuilder sb = new StringBuilder("Matriz de adyacencia (km, '-' = sin tramo directo)\n     ");
        for (int v = 0; v < nombres.length; v++) {
            sb.append(String.format("%5s", "v" + v));
        }
        sb.append('\n');
        for (int u = 0; u < nombres.length; u++) {
            sb.append(String.format("%5s", "v" + u));
            for (int v = 0; v < nombres.length; v++) {
                sb.append(String.format("%5s", matriz[u][v] == INFINITO ? "-" : String.valueOf(matriz[u][v])));
            }
            sb.append("   ").append(nombres[u]).append('\n');
        }
        return sb.toString();
    }
}
