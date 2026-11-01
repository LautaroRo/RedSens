package redsens.m1;

import redsens.modelo.Estacion;
import redsens.m6.Cronometro;

// experimento obligatorio del M1
public final class ExperimentoHash {

    private ExperimentoHash() {
    }

    public static String ejecutar(Estacion[] estaciones, int tamanioInicial) {
        // Con 10.000 estaciones se usan todos los códigos CBA-0000..9999, así que no queda ningún
        // CBA-NNNN libre para las búsquedas fallidas: se buscan códigos XYZ-NNNN, que no están.
        String[] ausentes = new String[estaciones.length];
        for (int i = 0; i < estaciones.length; i++) {
            ausentes[i] = String.format("XYZ-%04d", estaciones[i].getNumero());
        }

        DiccionarioEstaciones[] configuraciones = {
            new TablaHash(tamanioInicial, TablaHash.Exploracion.LINEAL),
            new TablaHash(tamanioInicial, TablaHash.Exploracion.CUADRATICA),
            new TablaHashEncadenada(tamanioInicial),
            new TablaHash(tamanioInicial, TablaHash.Exploracion.LINEAL, 0.9, TablaHash.FuncionHash.HORNER_MEZCLADO),
            new TablaHash(tamanioInicial, TablaHash.Exploracion.CUADRATICA, 0.9, TablaHash.FuncionHash.HORNER_MEZCLADO),
            // Comparación de funciones hash con la misma estrategia
            new TablaHash(tamanioInicial, TablaHash.Exploracion.LINEAL, 0.5, TablaHash.FuncionHash.HORNER),
            new TablaHash(tamanioInicial, TablaHash.Exploracion.LINEAL, 0.9, TablaHash.FuncionHash.HORNER),
            new TablaHash(tamanioInicial, TablaHash.Exploracion.LINEAL, 0.5, TablaHash.FuncionHash.SUMA_CARACTERES),
        };

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Experimento M1 - %,d estaciones, tamaño inicial %d%n", estaciones.length, tamanioInicial));
        sb.append(String.format("%-48s %7s %6s %6s %10s %8s %8s %8s %8s %9s%n", "Configuración", "Tamaño", "λ",
                "Rehash", "Colisiones", "Éxito", "Fracaso", "Teór. É", "Teór. F", "ns/busq."));

        for (DiccionarioEstaciones tabla : configuraciones) {
            for (Estacion e : estaciones) {
                tabla.insertar(e);
            }

            long sondeosExito = 0;
            for (Estacion e : estaciones) {
                tabla.buscar(e.getCodigo());
                sondeosExito += tabla.getSondeosUltimaOperacion();
            }
            long sondeosFracaso = 0;
            for (String codigo : ausentes) {
                tabla.buscar(codigo);
                sondeosFracaso += tabla.getSondeosUltimaOperacion();
            }
            long nanos = Cronometro.mejorDe(5, () -> {
                for (Estacion e : estaciones) {
                    tabla.buscar(e.getCodigo());
                }
            });

            double lambda = tabla.factorCarga();
            double[] teorico = teorico(tabla, lambda);
            sb.append(String.format("%-48s %7d %6.3f %6d %,10d %8.3f %8.3f %8s %8s %9.1f%n",
                    tabla.descripcion(), tabla.tamanio(), lambda, tabla.getRehashings(), tabla.getColisiones(),
                    (double) sondeosExito / estaciones.length, (double) sondeosFracaso / ausentes.length,
                    teorico == null ? "-" : String.format("%.3f", teorico[0]),
                    teorico == null ? "-" : String.format("%.3f", teorico[1]),
                    (double) nanos / estaciones.length));
        }
        sb.append("Éxito / Fracaso: sondeos promedio medidos por búsqueda exitosa y no exitosa.\n");
        sb.append("Teór.: lineal ½(1+1/(1-λ)) y ½(1+1/(1-λ)²); cuadrática ≈ -ln(1-λ)/λ y 1/(1-λ);"
                + " encadenamiento 1+λ/2 y λ (nodos comparados).\n");
        return sb.toString();
    }

    // valores esperados según las fórmulas de la lectura de análisis de tablas hash
    private static double[] teorico(DiccionarioEstaciones tabla, double lambda) {
        if (tabla instanceof TablaHashEncadenada) {
            return new double[] {1 + lambda / 2, lambda};
        }
        TablaHash t = (TablaHash) tabla;
        if (t.descripcion().contains("suma")) {
            return null;   // las fórmulas suponen una función hash que reparte bien
        }
        // Con direccionamiento abierto lo que pesa son las celdas ocupadas (incluye las borradas).
        double l = (double) t.celdasOcupadas() / t.tamanio();
        if (t.getExploracion() == TablaHash.Exploracion.LINEAL) {
            return new double[] {0.5 * (1 + 1 / (1 - l)), 0.5 * (1 + 1 / ((1 - l) * (1 - l)))};
        }
        return new double[] {-Math.log(1 - l) / l, 1 / (1 - l)};
    }
}
