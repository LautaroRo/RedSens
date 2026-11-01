package redsens.m5;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import redsens.m6.Cronometro;

// M5 - Calendario de guardias rotativas y experimento de tiempos de las dos implementaciones
public final class Guardias {

    public static final String[] TECNICOS = {"Ana", "Bruno", "Carla", "Diego", "Elena", "Facundo",
        "Gabriela", "Hernán", "Inés", "Julián", "Karina", "Lucas"};

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Guardias() {
    }

    // el orden de salida de Josephus es el orden de las guardias; el último que queda es el suplente
    public static String calendario(int pasadas, LocalDate desde) {
        int n = TECNICOS.length;
        int[] simple = JosephusSimple.ordenDeSalida(n, pasadas);
        int[] arbol = JosephusArbol.ordenDeSalida(n, pasadas);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Calendario de guardias - N = %d técnicos, M = %d%n", n, pasadas));
        sb.append("Ronda inicial: ");
        for (int i = 0; i < n; i++) {
            sb.append(i + 1).append('.').append(TECNICOS[i]).append(i < n - 1 ? "  " : "\n\n");
        }
        LocalDate sabado = desde.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        for (int i = 0; i < n - 1; i++) {
            sb.append(String.format("  Fin de semana %2d  (%s y %s)  ->  %2d. %s%n", i + 1, sabado.format(FECHA),
                    sabado.plusDays(1).format(FECHA), simple[i], TECNICOS[simple[i] - 1]));
            sabado = sabado.plusWeeks(1);
        }
        sb.append(String.format("  Suplente de emergencias          ->  %2d. %s%n", simple[n - 1],
                TECNICOS[simple[n - 1] - 1]));
        sb.append("Las dos implementaciones dan el mismo orden: ")
                .append(java.util.Arrays.equals(simple, arbol) ? "sí" : "NO").append('\n');
        return sb.toString();
    }

    // caso de la lectura
    public static String casoVerificacion() {
        int[] simple = JosephusSimple.ordenDeSalida(5, 1);
        int[] arbol = JosephusArbol.ordenDeSalida(5, 1);
        int[] esperado = {2, 4, 1, 5, 3};
        return String.format("Caso de verificación N = 5, M = 1 (esperado: salen 2, 4, 1, 5 y queda 3)%n"
                + "  Lista + iterador: %s -> %s%n  Árbol con rangos: %s -> %s%n",
                java.util.Arrays.toString(simple), java.util.Arrays.equals(simple, esperado) ? "correcto" : "INCORRECTO",
                java.util.Arrays.toString(arbol), java.util.Arrays.equals(arbol, esperado) ? "correcto" : "INCORRECTO");
    }

    // experimento obligatorio del M5
    public static String experimento(int mGrupo) {
        int[] tamanios = {1000, 10000, 100000};
        int[] valoresM = {mGrupo, 100, 1000};
        JosephusSimple.ordenDeSalida(2000, 3);      // calentamiento de la JVM
        JosephusArbol.ordenDeSalida(2000, 3);

        StringBuilder sb = new StringBuilder("Experimento M5 - tiempo de las dos implementaciones de Josephus\n");
        sb.append(String.format("%8s %6s %16s %16s %10s %10s%n", "N", "M", "Lista O(N*M)", "Árbol O(N log N)",
                "Cociente", "Último"));
        for (int m : valoresM) {
            for (int n : tamanios) {
                int[][] resultado = new int[2][];
                long tSimple = Cronometro.mejorDe(3, () -> resultado[0] = JosephusSimple.ordenDeSalida(n, m));
                long tArbol = Cronometro.mejorDe(3, () -> resultado[1] = JosephusArbol.ordenDeSalida(n, m));
                boolean iguales = java.util.Arrays.equals(resultado[0], resultado[1]);
                sb.append(String.format("%,8d %6d %16s %16s %9.1fx %10s%n", n, m, Cronometro.formatear(tSimple),
                        Cronometro.formatear(tArbol), (double) tSimple / tArbol,
                        iguales ? String.valueOf(resultado[0][n - 1]) : "DISTINTOS"));
            }
        }
        sb.append("Cociente = tiempo lista / tiempo árbol (mayor que 1: el árbol es más rápido).\n");
        return sb.toString();
    }
}
