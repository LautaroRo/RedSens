package redsens.m3;

import java.util.ArrayList;
import java.util.List;

// M3 - BFS, DFS, Dijkstra y Floyd sobre cualquier representación del grafo
public final class AlgoritmosGrafo {

    public record ResultadoDijkstra(int origen, int[] distancia, int[] previo) {
    }

    public record ResultadoFloyd(int[][] distancia, int[][] siguiente) {
    }

    private AlgoritmosGrafo() {
    }

    // ---------- BFS ----------

    // recorrido en anchura
    public static int[] bfs(Grafo g, int origen) {
        int n = g.cantidadVertices();
        int[] nivel = new int[n];
        java.util.Arrays.fill(nivel, -1);
        int[] cola = new int[n];        // cada vértice entra a lo sumo una vez
        int frente = 0;
        int fin = 0;
        nivel[origen] = 0;
        cola[fin++] = origen;
        while (frente < fin) {
            int v = cola[frente++];
            for (int w : g.vecinos(v)) {
                if (nivel[w] == -1) {
                    nivel[w] = nivel[v] + 1;
                    cola[fin++] = w;
                }
            }
        }
        return nivel;
    }

    // ---------- DFS ----------

    // recorrido en profundidad recursivo desde el origen; devuelve el orden de visita
    public static List<Integer> dfs(Grafo g, int origen, boolean[] visitado) {
        List<Integer> orden = new ArrayList<>();
        dfs(g, origen, visitado, orden);
        return orden;
    }

    private static void dfs(Grafo g, int v, boolean[] visitado, List<Integer> orden) {
        visitado[v] = true;
        orden.add(v);
        for (int w : g.vecinos(v)) {
            if (!visitado[w]) {
                dfs(g, w, visitado, orden);
            }
        }
    }

    // conexo = un DFS desde cualquier vértice llega a todos
    public static boolean esConexo(Grafo g) {
        boolean[] visitado = new boolean[g.cantidadVertices()];
        return dfs(g, 0, visitado).size() == g.cantidadVertices();
    }

    // ---------- Dijkstra ----------

    // dijkstra con arreglos
    public static ResultadoDijkstra dijkstra(Grafo g, int origen) {
        int n = g.cantidadVertices();
        int[] distancia = new int[n];
        int[] previo = new int[n];
        boolean[] fijado = new boolean[n];
        java.util.Arrays.fill(distancia, Grafo.INFINITO);
        java.util.Arrays.fill(previo, -1);
        distancia[origen] = 0;

        for (int paso = 0; paso < n; paso++) {
            int v = -1;
            for (int i = 0; i < n; i++) {
                if (!fijado[i] && (v == -1 || distancia[i] < distancia[v])) {
                    v = i;
                }
            }
            if (distancia[v] == Grafo.INFINITO) {
                break;                              // lo que queda no es alcanzable
            }
            fijado[v] = true;                       // su distancia ya no puede mejorar (pesos ≥ 0)
            for (int w : g.vecinos(v)) {
                int nueva = distancia[v] + g.peso(v, w);
                if (!fijado[w] && nueva < distancia[w]) {
                    distancia[w] = nueva;
                    previo[w] = v;
                }
            }
        }
        return new ResultadoDijkstra(origen, distancia, previo);
    }

    // reconstruye el camino siguiendo los previos desde el destino hacia atrás
    public static List<Integer> camino(ResultadoDijkstra r, int destino) {
        List<Integer> camino = new ArrayList<>();
        if (r.distancia()[destino] == Grafo.INFINITO) {
            return camino;
        }
        for (int v = destino; v != -1; v = r.previo()[v]) {
            camino.add(0, v);
        }
        return camino;
    }

    // ---------- Floyd ----------

    // floyd
    public static ResultadoFloyd floyd(Grafo g) {
        int n = g.cantidadVertices();
        int[][] d = new int[n][n];
        int[][] siguiente = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = g.peso(i, j);
                siguiente[i][j] = d[i][j] == Grafo.INFINITO ? -1 : j;
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        siguiente[i][j] = siguiente[i][k];
                    }
                }
            }
        }
        return new ResultadoFloyd(d, siguiente);
    }

    public static List<Integer> caminoFloyd(ResultadoFloyd f, int desde, int hasta) {
        List<Integer> camino = new ArrayList<>();
        if (f.siguiente()[desde][hasta] == -1) {
            return camino;
        }
        camino.add(desde);
        while (desde != hasta) {
            desde = f.siguiente()[desde][hasta];
            camino.add(desde);
        }
        return camino;
    }

    public static String nombres(Grafo g, List<Integer> camino) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(g.nombre(camino.get(i)));
        }
        return sb.toString();
    }
}
