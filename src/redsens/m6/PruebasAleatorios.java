package redsens.m6;

import java.util.Random;

// M6 - Verificación del generador de números aleatorios, como en la lectura de simulación
public final class PruebasAleatorios {

    // valor crítico de chi-cuadrado con 100 grados de libertad y 5 % de significación
    private static final double CHI2_CRITICO_GL100 = 124.342;

    private PruebasAleatorios() {
    }

    // dos generadores con la misma semilla deben dar exactamente la misma secuencia
    public static boolean secuenciasIguales(long semilla, int cantidad) {
        Random a = new Random(semilla);
        Random b = new Random(semilla);
        for (int i = 0; i < cantidad; i++) {
            if (a.nextInt() != b.nextInt()) {
                return false;
            }
        }
        return true;
    }

    // con semillas distintas, las secuencias tienen que diferir en algún valor
    public static boolean secuenciasDistintas(long semilla1, long semilla2, int cantidad) {
        Random a = new Random(semilla1);
        Random b = new Random(semilla2);
        for (int i = 0; i < cantidad; i++) {
            if (a.nextInt() != b.nextInt()) {
                return true;
            }
        }
        return false;
    }

    // genera niveles de batería 0..100 y cuenta cuántas veces sale cada valor
    public static String compruebaUniformidad(long semilla, int muestras) {
        Random random = new Random(semilla);
        int[] conteo = new int[101];
        for (int i = 0; i < muestras; i++) {
            conteo[random.nextInt(101)]++;
        }
        double esperado = muestras / 101.0;

        double chi2 = 0;
        double maxDesvio = 0;
        for (int c : conteo) {
            double d = c - esperado;
            chi2 += d * d / esperado;
            maxDesvio = Math.max(maxDesvio, Math.abs(d) / esperado);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Uniformidad de la batería (0 a 100) con %,d muestras, semilla %d%n", muestras, semilla));
        sb.append(String.format("  %-10s %10s %10s %9s%n", "Intervalo", "Observado", "Esperado", "Desvío"));
        for (int desde = 0; desde <= 100; desde += 10) {
            int hasta = desde == 90 ? 100 : desde + 9;
            int observado = 0;
            for (int v = desde; v <= hasta; v++) {
                observado += conteo[v];
            }
            double esp = esperado * (hasta - desde + 1);
            sb.append(String.format("  %3d - %3d  %10d %10.0f %8.2f%%%n", desde, hasta, observado, esp,
                    100 * (observado - esp) / esp));
            if (desde == 90) {
                break;
            }
        }
        sb.append(String.format("  Por valor: esperado %.1f apariciones, desvío máximo %.2f%%%n", esperado, 100 * maxDesvio));
        sb.append(String.format("  Chi-cuadrado = %.2f (100 grados de libertad, crítico al 5%% = %.2f) -> %s%n", chi2,
                CHI2_CRITICO_GL100, chi2 < CHI2_CRITICO_GL100 ? "no se rechaza la uniformidad" : "se rechaza la uniformidad"));
        return sb.toString();
    }

    public static String informe(long semilla) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("secuenciasIguales(%d, 1000)        = %b%n", semilla, secuenciasIguales(semilla, 1000)));
        sb.append(String.format("secuenciasDistintas(%d, %d, 1000) = %b%n", semilla, semilla + 1,
                secuenciasDistintas(semilla, semilla + 1, 1000)));
        Random a = new Random(semilla);
        Random b = new Random(semilla);
        Random c = new Random(semilla + 1);
        sb.append("Primeros 5 valores nextInt(101):\n");
        sb.append("  semilla ").append(semilla).append("     :");
        for (int i = 0; i < 5; i++) {
            sb.append(String.format(" %3d", a.nextInt(101)));
        }
        sb.append("\n  semilla ").append(semilla).append(" (2.º):");
        for (int i = 0; i < 5; i++) {
            sb.append(String.format(" %3d", b.nextInt(101)));
        }
        sb.append("\n  semilla ").append(semilla + 1).append("     :");
        for (int i = 0; i < 5; i++) {
            sb.append(String.format(" %3d", c.nextInt(101)));
        }
        sb.append("\n\n").append(compruebaUniformidad(semilla, 1_000_000));
        return sb.toString();
    }
}
