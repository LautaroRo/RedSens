package redsens.modelo;

// parámetros asignados al grupo (sección 3 del enunciado)
public final class Parametros {

    // número de grupo confirmado en Canvas (1 a 15)
    public static final int GRUPO = 2;

    private static final long[] SEMILLAS = {1026, 2026, 3026, 4026, 5026, 6026, 7026, 8026,
        9026, 10026, 11026, 12026, 13026, 14026, 15026};
    private static final int[] TAMANIOS_TABLA = {11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67};
    private static final String[] BASES = {"Río Cuarto", "Villa María", "San Francisco", "Villa Dolores", "Deán Funes"};
    private static final int[] M = {1, 2, 3, 4};

    public static final long SEMILLA = SEMILLAS[GRUPO - 1];
    public static final int TAMANIO_INICIAL_TABLA = TAMANIOS_TABLA[GRUPO - 1];
    public static final String BASE_CUADRILLA = BASES[(GRUPO - 1) % BASES.length];
    public static final int M_JOSEPHUS = M[(GRUPO - 1) % M.length];

    // base de control común a todos los grupos, para verificar Dijkstra
    public static final String BASE_CONTROL = "Córdoba";

    public static final String ARCHIVO_RUTAS = "datos/rutas.txt";
    public static final String CARPETA_DATOS = "datos";
    public static final String CARPETA_RESULTADOS = "resultados";

    private Parametros() {
    }

    public static String resumen() {
        return String.format("Grupo %d - semilla %d - tabla hash inicial %d - base %s - M de Josephus %d",
                GRUPO, SEMILLA, TAMANIO_INICIAL_TABLA, BASE_CUADRILLA, M_JOSEPHUS);
    }
}
