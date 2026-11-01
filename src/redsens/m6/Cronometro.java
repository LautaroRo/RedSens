package redsens.m6;

// M6 - Medición de tiempos con System.nanoTime
public final class Cronometro {

    private Cronometro() {
    }

    // ejecuta la tarea y devuelve cuántos nanosegundos tardó
    public static long medir(Runnable tarea) {
        long inicio = System.nanoTime();
        tarea.run();
        return System.nanoTime() - inicio;
    }

    // ejecuta la tarea varias veces y se queda con el menor tiempo
    public static long mejorDe(int repeticiones, Runnable tarea) {
        long mejor = Long.MAX_VALUE;
        for (int i = 0; i < repeticiones; i++) {
            mejor = Math.min(mejor, medir(tarea));
        }
        return mejor;
    }

    public static String formatear(long nanos) {
        if (nanos < 10_000) {
            return nanos + " ns";
        }
        if (nanos < 10_000_000) {
            return String.format("%.1f µs", nanos / 1e3);
        }
        return String.format("%.1f ms", nanos / 1e6);
    }
}
