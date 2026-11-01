package redsens.m3;

import java.util.ArrayList;
import java.util.List;

// M3 - Plan de visitas de la cuadrilla
public final class PlanVisitas {

    // orden de visita (empieza y termina en la base) y km totales
    public record Plan(List<Integer> orden, int kmTotales) {
    }

    private PlanVisitas() {
    }

    // vecino más cercano
    public static Plan vecinoMasCercano(int[][] distancia, int base, boolean[] aVisitar) {
        int n = distancia.length;
        boolean[] pendiente = aVisitar.clone();
        pendiente[base] = false;
        List<Integer> orden = new ArrayList<>();
        orden.add(base);
        int actual = base;
        int total = 0;
        while (true) {
            int elegido = -1;
            for (int v = 0; v < n; v++) {
                if (pendiente[v] && (elegido == -1 || distancia[actual][v] < distancia[actual][elegido])) {
                    elegido = v;
                }
            }
            if (elegido == -1) {
                break;
            }
            total += distancia[actual][elegido];
            pendiente[elegido] = false;
            orden.add(elegido);
            actual = elegido;
        }
        total += distancia[actual][base];
        orden.add(base);
        return new Plan(orden, total);
    }

    // recorrido óptimo, solo para comparar
    public static Plan optimo(int[][] distancia, int base, boolean[] aVisitar) {
        int[] destinos = new int[distancia.length];
        int k = 0;
        for (int v = 0; v < distancia.length; v++) {
            if (aVisitar[v] && v != base) {
                destinos[k++] = v;
            }
        }
        if (k == 0) {
            return new Plan(List.of(base, base), 0);
        }
        int completos = 1 << k;
        // costo[S][j]: menor km saliendo de la base, visitando el conjunto S y terminando en destinos[j]
        int[][] costo = new int[completos][k];
        int[][] desde = new int[completos][k];
        for (int[] fila : costo) {
            java.util.Arrays.fill(fila, Grafo.INFINITO);
        }
        for (int j = 0; j < k; j++) {
            costo[1 << j][j] = distancia[base][destinos[j]];
            desde[1 << j][j] = -1;
        }
        for (int s = 1; s < completos; s++) {
            for (int j = 0; j < k; j++) {
                if ((s & (1 << j)) == 0 || costo[s][j] == Grafo.INFINITO) {
                    continue;
                }
                for (int siguiente = 0; siguiente < k; siguiente++) {
                    if ((s & (1 << siguiente)) != 0) {
                        continue;
                    }
                    int t = s | (1 << siguiente);
                    int c = costo[s][j] + distancia[destinos[j]][destinos[siguiente]];
                    if (c < costo[t][siguiente]) {
                        costo[t][siguiente] = c;
                        desde[t][siguiente] = j;
                    }
                }
            }
        }
        int mejor = -1;
        int mejorCosto = Grafo.INFINITO;
        for (int j = 0; j < k; j++) {
            int c = costo[completos - 1][j] + distancia[destinos[j]][base];
            if (c < mejorCosto) {
                mejorCosto = c;
                mejor = j;
            }
        }
        List<Integer> orden = new ArrayList<>();
        orden.add(base);
        int s = completos - 1;
        for (int j = mejor; j != -1; ) {
            orden.add(1, destinos[j]);
            int anterior = desde[s][j];
            s &= ~(1 << j);
            j = anterior;
        }
        orden.add(base);
        return new Plan(orden, mejorCosto);
    }
}
