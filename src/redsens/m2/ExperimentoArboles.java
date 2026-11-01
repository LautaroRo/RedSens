package redsens.m2;

import java.util.Arrays;
import java.util.Comparator;

import redsens.modelo.Estacion;
import redsens.m6.Cronometro;

// experimento obligatorio del M2
public final class ExperimentoArboles {

    private static final int[] TAMANIOS = {1000, 5000, 10000};

    private ExperimentoArboles() {
    }

    // al menos 10.000 estaciones en orden aleatorio (las del generador)
    public static String ejecutar(Estacion[] estaciones) {
        // Calentamiento: la primera vez la JVM interpreta el código; después lo compila (JIT).
        medir(Arrays.copyOf(estaciones, 2000), new ABB());
        medir(Arrays.copyOf(estaciones, 2000), new AVL());

        StringBuilder sb = new StringBuilder();
        sb.append("Experimento M2 - altura y tiempo de búsqueda (se buscan todas las claves, en orden mezclado)\n");
        sb.append(String.format("%-11s %7s %-5s %7s %13s %15s %16s%n", "Orden", "N", "Árbol", "Altura",
                "log2(N)", "Inserción", "Búsqueda prom."));
        for (String orden : new String[] {"ascendente", "aleatorio"}) {
            for (int n : TAMANIOS) {
                Estacion[] datos = Arrays.copyOf(estaciones, n);
                if (orden.equals("ascendente")) {
                    Arrays.sort(datos, Comparator.comparingInt(Estacion::getNumero));
                }
                for (ArbolBinarioBusqueda arbol : new ArbolBinarioBusqueda[] {new ABB(), new AVL()}) {
                    long[] tiempos = medir(datos, arbol);
                    sb.append(String.format("%-11s %,7d %-5s %7d %13.2f %15s %16s%n", orden, n, arbol.nombre(),
                            arbol.altura(), Math.log(n) / Math.log(2), Cronometro.formatear(tiempos[0]),
                            Cronometro.formatear(tiempos[1] / n)));
                }
            }
        }
        sb.append("Altura en aristas (una hoja sola tiene altura 0). Cota del AVL: altura < 1,44*log2(N+2).\n");
        return sb.toString();
    }

    // devuelve {tiempo de insertar todo, tiempo de buscar todo}
    private static long[] medir(Estacion[] datos, ArbolBinarioBusqueda arbol) {
        long insercion = Cronometro.medir(() -> {
            for (Estacion e : datos) {
                arbol.insertar(e);
            }
        });
        // Se busca en un orden distinto al de inserción para no favorecer a ningún árbol.
        Estacion[] consultas = datos.clone();
        Arrays.sort(consultas, Comparator.comparingInt(e -> (e.getNumero() * 7919) % 10007));
        long busqueda = Cronometro.mejorDe(3, () -> {
            for (Estacion e : consultas) {
                arbol.buscar(e.getNumero());
            }
        });
        return new long[] {insercion, busqueda};
    }
}
