package redsens;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import redsens.m1.TablaHash;
import redsens.m2.ABB;
import redsens.m2.AVL;
import redsens.m3.AlgoritmosGrafo;
import redsens.m3.CargadorGrafo;
import redsens.m3.Grafo;
import redsens.m3.PlanVisitas;
import redsens.m4.ArchivoHuffman;
import redsens.m4.Huffman;
import redsens.m5.Guardias;
import redsens.m5.JosephusArbol;
import redsens.m5.JosephusSimple;
import redsens.m6.GeneradorDatos;
import redsens.m6.InformeRendimiento;
import redsens.m6.PruebasAleatorios;
import redsens.modelo.Estacion;
import redsens.modelo.Parametros;

// redSens - menú principal que integra los seis módulos
public class Main {

    private static final int FILAS_MAXIMAS = 40;

    private final Consola consola = new Consola();
    private CargadorGrafo.RedVial red;
    private Grafo grafo;                        // representación que usan los algoritmos
    private AlgoritmosGrafo.ResultadoFloyd floyd;

    private Estacion[] estaciones;
    private TablaHash registro;                 // M1: búsqueda por código
    private AVL indice;                         // M2: orden, rangos y críticas

    // el programa corre en un hilo con 512 MB de pila
    public static void main(String[] args) throws InterruptedException {
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag("es-AR"));   // 10.000 y 0,389
        Thread hilo = new Thread(null, () -> {
            try {
                new Main().iniciar(args);
            } catch (IOException e) {
                System.out.println("Error de archivo: " + e.getMessage());
            }
        }, "redsens", 512L * 1024 * 1024);
        hilo.start();
        hilo.join();
    }

    private void iniciar(String[] args) throws IOException {
        red = CargadorGrafo.cargar(Path.of(Parametros.ARCHIVO_RUTAS));
        grafo = red.lista();
        floyd = AlgoritmosGrafo.floyd(grafo);

        if (args.length > 0 && args[0].equals("--experimentos")) {
            String informe = InformeRendimiento.ejecutar(localidades());
            System.out.println(informe);
            System.out.println("Guardado en " + InformeRendimiento.guardar(informe));
            return;
        }
        menuPrincipal();
    }

    private String[] localidades() {
        String[] nombres = new String[grafo.cantidadVertices()];
        for (int i = 0; i < nombres.length; i++) {
            nombres[i] = grafo.nombre(i);
        }
        return nombres;
    }

    // =====================================================================
    // Menú principal
    // =====================================================================

    private void menuPrincipal() throws IOException {
        System.out.println("\nRedSens - Sistema de gestión de la red de estaciones de DATA Investigaciones");
        System.out.println(Parametros.resumen());
        while (true) {
            System.out.println("\n================ MENÚ PRINCIPAL ================");
            System.out.println(" 1. Cargar la red (generar estaciones -> tabla hash + AVL)");
            System.out.println(" 2. Buscar una estación por código           (M1)");
            System.out.println(" 3. Dar de alta / de baja una estación       (M1 + M2)");
            System.out.println(" 4. Listar estaciones por rango de números   (M2)");
            System.out.println(" 5. Reporte de críticas y plan de visitas    (M2 + M3)");
            System.out.println(" 6. Red vial: BFS, DFS, Dijkstra, Floyd      (M3)");
            System.out.println(" 7. Mediciones del día: comprimir y verificar (M4)");
            System.out.println(" 8. Calendario de guardias                   (M5)");
            System.out.println(" 9. Pruebas del generador aleatorio          (M6)");
            System.out.println("10. Batería de experimentos de rendimiento   (M6)");
            System.out.println("11. Demos paso a paso (AVL, hash, Huffman, Josephus)");
            System.out.println(" 0. Salir");
            int opcion = consola.leerEntero("Opción", 0, 11, 0);
            switch (opcion) {
                case 0:
                    return;
                case 1:
                    cargarRed(consola.leerEntero("Cantidad de estaciones (1 a 10000)", 1, 10000, 1000));
                    break;
                case 2:
                    menuBuscar();
                    break;
                case 3:
                    menuAltaBaja();
                    break;
                case 4:
                    menuRango();
                    break;
                case 5:
                    asegurarRed();
                    reporteCriticasYPlan();
                    break;
                case 6:
                    menuGrafo();
                    break;
                case 7:
                    asegurarRed();
                    mediciones(consola.leerEntero("Cantidad de líneas", 1, 100000, 5000));
                    break;
                case 8:
                    System.out.println(Guardias.casoVerificacion());
                    System.out.println(Guardias.calendario(Parametros.M_JOSEPHUS, LocalDate.now()));
                    break;
                case 9:
                    System.out.println(PruebasAleatorios.informe(Parametros.SEMILLA));
                    break;
                case 10:
                    experimentos();
                    break;
                case 11:
                    menuDemos();
                    break;
                default:
                    break;
            }
            consola.pausa();
        }
    }

    // =====================================================================
    // M1 + M2: carga, búsqueda, alta y baja, rango
    // =====================================================================

    private void cargarRed(int n) {
        estaciones = new GeneradorDatos(Parametros.SEMILLA).generarEstaciones(n, localidades());
        registro = new TablaHash(Parametros.TAMANIO_INICIAL_TABLA, TablaHash.Exploracion.LINEAL);
        indice = new AVL();
        for (Estacion e : estaciones) {
            registro.insertar(e);
            indice.insertar(e);
        }
        System.out.printf("Red cargada: %,d estaciones (semilla %d).%n", n, Parametros.SEMILLA);
        System.out.printf("  Tabla hash: %s, tamaño %,d, λ = %.3f, %d rehashings, %,d colisiones%n",
                registro.descripcion(), registro.tamanio(), registro.factorCarga(), registro.getRehashings(),
                registro.getColisiones());
        System.out.printf("  AVL: %,d nodos, %,d hojas, altura %d (log2 n = %.1f), %d rotaciones simples y %d dobles%n",
                indice.cantidadNodos(), indice.cantidadHojas(), indice.altura(), Math.log(n) / Math.log(2),
                indice.getRotacionesSimples(), indice.getRotacionesDobles());
        int mostrar = Math.min(5, n);
        System.out.println("  Primeras " + mostrar + " generadas:");
        for (int i = 0; i < mostrar; i++) {
            System.out.println("    " + estaciones[i]);
        }
    }

    private void asegurarRed() {
        if (registro == null) {
            System.out.println("(Todavía no se cargó la red: se cargan 1.000 estaciones.)");
            cargarRed(1000);
        }
    }

    private void menuBuscar() {
        asegurarRed();
        String codigo = consola.leerTexto("Código (por ejemplo CBA-0347, o solo 347): ").toUpperCase();
        buscar(normalizarCodigo(codigo));
    }

    private static String normalizarCodigo(String texto) {
        if (texto.matches("\\d{1,4}")) {
            return Estacion.codigoDe(Integer.parseInt(texto));
        }
        return texto;
    }

    private void buscar(String codigo) {
        long inicio = System.nanoTime();
        Estacion e = registro.buscar(codigo);
        long nanos = System.nanoTime() - inicio;
        System.out.printf("h(%s) = %d - %d sondeo(s) - %d ns%n", codigo, registro.hash(codigo),
                registro.getSondeosUltimaOperacion(), nanos);
        System.out.println(e == null ? "No existe una estación con ese código." : e.toString());
    }

    private void menuAltaBaja() {
        asegurarRed();
        int opcion = consola.leerEntero("1 = alta, 2 = baja", 1, 2, 1);
        if (opcion == 2) {
            String codigo = normalizarCodigo(consola.leerTexto("Código a dar de baja: ").toUpperCase());
            baja(codigo);
            return;
        }
        int numero = consola.leerEntero("Número de estación (0 a 9999)", 0, 9999, 0);
        if (registro.buscar(Estacion.codigoDe(numero)) != null) {
            System.out.println("Ya existe " + Estacion.codigoDe(numero) + ".");
            return;
        }
        String nombre = consola.leerTexto("Nombre: ");
        listarLocalidades();
        int localidad = consola.leerEntero("Localidad", 0, grafo.cantidadVertices() - 1, 0);
        int tipo = consola.leerEntero("Tipo (0 pluviométrica, 1 meteorológica, 2 hidrométrica)", 0, 2, 0);
        int bateria = consola.leerEntero("Batería (%)", 0, 100, 100);
        int dias = consola.leerEntero("Días sin visita", 0, 10000, 0);
        Estacion e = new Estacion(numero, nombre.isEmpty() ? "Estación nueva" : nombre, grafo.nombre(localidad),
                GeneradorDatos.TIPOS[tipo], bateria, dias);
        boolean enHash = registro.insertar(e);
        boolean enAvl = indice.insertar(e);
        System.out.printf("Alta de %s: tabla hash %s (%d sondeos), AVL %s.%n", e.getCodigo(), enHash ? "ok" : "falló",
                registro.getSondeosUltimaOperacion(), enAvl ? "ok" : "falló");
    }

    private void baja(String codigo) {
        Estacion e = registro.buscar(codigo);
        if (e == null) {
            System.out.println("No existe " + codigo + ".");
            return;
        }
        registro.eliminar(codigo);      // perezosa: la celda queda marcada como borrada
        indice.eliminar(e.getNumero());
        System.out.printf("Baja de %s. En la tabla hash la celda queda marcada como borrada "
                + "(celdas ocupadas: %d, activas: %d).%n", codigo, registro.celdasOcupadas(), registro.cantidad());
    }

    private void menuRango() {
        asegurarRed();
        int a = consola.leerEntero("Desde el número", 0, 9999, 100);
        int b = consola.leerEntero("Hasta el número", a, 9999, Math.min(9999, a + 100));
        listarRango(a, b);
    }

    private void listarRango(int a, int b) {
        List<Estacion> resultado = new ArrayList<>();
        indice.rango(a, b, resultado);
        System.out.printf("Estaciones con número entre %d y %d: %d%n", a, b, resultado.size());
        imprimir(resultado);
    }

    private static void imprimir(List<Estacion> lista) {
        for (int i = 0; i < lista.size() && i < FILAS_MAXIMAS; i++) {
            System.out.println("  " + lista.get(i));
        }
        if (lista.size() > FILAS_MAXIMAS) {
            System.out.printf("  ... y %d más.%n", lista.size() - FILAS_MAXIMAS);
        }
    }

    // =====================================================================
    // M2 -> M3: críticas y plan de visitas
    // =====================================================================

    private void reporteCriticasYPlan() {
        List<Estacion> criticas = new ArrayList<>();
        indice.criticas(criticas);
        System.out.printf("Estaciones críticas (batería < %d%%), ordenadas por número: %d de %d%n",
                Estacion.UMBRAL_CRITICO, criticas.size(), indice.cantidadNodos());
        imprimir(criticas);

        int[] porLocalidad = new int[grafo.cantidadVertices()];
        for (Estacion e : criticas) {
            porLocalidad[grafo.indiceDe(e.getLocalidad())]++;
        }
        boolean[] aVisitar = new boolean[grafo.cantidadVertices()];
        System.out.println("\nCríticas por localidad:");
        for (int v = 0; v < porLocalidad.length; v++) {
            if (porLocalidad[v] > 0) {
                aVisitar[v] = true;
                System.out.printf("  %-17s %4d%n", grafo.nombre(v), porLocalidad[v]);
            }
        }
        planDeVisitas(grafo.indiceDe(Parametros.BASE_CUADRILLA), aVisitar);
    }

    private void planDeVisitas(int base, boolean[] aVisitar) {
        boolean hayQueSalir = false;
        for (int v = 0; v < aVisitar.length; v++) {
            hayQueSalir |= aVisitar[v] && v != base;
        }
        if (!hayQueSalir) {
            System.out.println("\nNo hay estaciones críticas fuera de " + grafo.nombre(base)
                    + ": la cuadrilla no necesita salir de la base.");
            return;
        }
        int[][] d = floyd.distancia();
        PlanVisitas.Plan plan = PlanVisitas.vecinoMasCercano(d, base, aVisitar);
        System.out.println("\nPlan de visitas desde " + grafo.nombre(base) + " (vecino más cercano sobre la tabla de Floyd):");
        imprimirPlan(plan);

        PlanVisitas.Plan optimo = PlanVisitas.optimo(d, base, aVisitar);
        System.out.println("\nPara comparar, el recorrido óptimo (búsqueda exhaustiva con programación dinámica):");
        imprimirPlan(optimo);
        int diferencia = plan.kmTotales() - optimo.kmTotales();
        System.out.printf("%nEl vecino más cercano hace %d km más que el óptimo (%.1f%%).%n", diferencia,
                100.0 * diferencia / optimo.kmTotales());
        System.out.println("No garantiza el óptimo porque elige siempre el paso más corto desde donde está, sin mirar"
                + " cuánto le va a costar lo que queda (en especial la vuelta a la base).");
    }

    private void imprimirPlan(PlanVisitas.Plan plan) {
        List<Integer> orden = plan.orden();
        int[][] d = floyd.distancia();
        for (int i = 0; i + 1 < orden.size(); i++) {
            int desde = orden.get(i);
            int hasta = orden.get(i + 1);
            List<Integer> camino = AlgoritmosGrafo.caminoFloyd(floyd, desde, hasta);
            System.out.printf("  %2d. %-17s -> %-17s %4d km   (%s)%n", i + 1, grafo.nombre(desde), grafo.nombre(hasta),
                    d[desde][hasta], AlgoritmosGrafo.nombres(grafo, camino));
        }
        System.out.printf("  Total: %d km%n", plan.kmTotales());
    }

    // =====================================================================
    // M3: red vial
    // =====================================================================

    private void menuGrafo() {
        while (true) {
            System.out.println("\n---------- RED VIAL (algoritmos sobre la " + grafo.nombreRepresentacion() + ") ----------");
            System.out.println(" 1. Ver las dos representaciones");
            System.out.println(" 2. BFS: localidades a 1, 2 y 3 tramos");
            System.out.println(" 3. DFS: conexidad y corte de una ruta");
            System.out.println(" 4. Dijkstra: distancia y camino mínimo");
            System.out.println(" 5. Floyd: matriz de distancias mínimas");
            System.out.println(" 6. Cambiar la representación que usan los algoritmos");
            System.out.println(" 0. Volver");
            int opcion = consola.leerEntero("Opción", 0, 6, 0);
            switch (opcion) {
                case 0:
                    return;
                case 1:
                    representaciones();
                    break;
                case 2:
                    bfs(elegirLocalidad(Parametros.BASE_CUADRILLA));
                    break;
                case 3:
                    conexidadYCortes();
                    break;
                case 4:
                    dijkstra(elegirLocalidad(Parametros.BASE_CUADRILLA));
                    break;
                case 5:
                    matrizFloyd();
                    break;
                case 6:
                    grafo = grafo == red.lista() ? red.matriz() : red.lista();
                    floyd = AlgoritmosGrafo.floyd(grafo);
                    System.out.println("Ahora se usa la " + grafo.nombreRepresentacion() + ".");
                    break;
                default:
                    break;
            }
            consola.pausa();
        }
    }

    private void listarLocalidades() {
        for (int v = 0; v < grafo.cantidadVertices(); v++) {
            System.out.printf("  %2d. %s%n", v, grafo.nombre(v));
        }
    }

    private int elegirLocalidad(String porDefecto) {
        listarLocalidades();
        return consola.leerEntero("Origen", 0, grafo.cantidadVertices() - 1, grafo.indiceDe(porDefecto));
    }

    private void representaciones() {
        System.out.println(red.matriz().mostrar());
        System.out.println(red.lista().mostrar());
        int v = grafo.cantidadVertices();
        int e = grafo.cantidadAristas();
        System.out.printf("%d vértices y %d aristas: densidad %d / %d = %.0f%% de los tramos posibles.%n", v, e, e,
                v * (v - 1) / 2, 100.0 * e / (v * (v - 1) / 2));
        System.out.printf("Matriz: %d celdas, %d con tramo. Lista: %d nodos (cada tramo aparece dos veces).%n",
                v * v, 2 * e, 2 * e);
        System.out.println("Conviene la lista para BFS, DFS y Dijkstra (recorren solo los vecinos reales: O(V+E) en vez"
                + " de O(V²)). La matriz conviene para Floyd y para consultar un tramo puntual en O(1). Con 12"
                + " localidades cualquiera de las dos es instantánea; con miles de localidades y pocas rutas por"
                + " localidad la matriz desperdiciaría casi toda la memoria.");
    }

    private void bfs(int origen) {
        int[] nivel = AlgoritmosGrafo.bfs(grafo, origen);
        System.out.println("BFS desde " + grafo.nombre(origen) + ":");
        for (int k = 1; k <= 3; k++) {
            StringBuilder sb = new StringBuilder();
            for (int v = 0; v < nivel.length; v++) {
                if (nivel[v] == k) {
                    sb.append(sb.length() > 0 ? ", " : "").append(grafo.nombre(v));
                }
            }
            System.out.printf("  A %d tramo(s): %s%n", k, sb.length() == 0 ? "(ninguna)" : sb);
        }
        StringBuilder resto = new StringBuilder();
        for (int v = 0; v < nivel.length; v++) {
            if (nivel[v] > 3) {
                resto.append(resto.length() > 0 ? ", " : "").append(grafo.nombre(v)).append(" (").append(nivel[v]).append(')');
            }
        }
        if (resto.length() > 0) {
            System.out.println("  A más de 3 tramos: " + resto);
        }
    }

    private void conexidadYCortes() {
        int base = grafo.indiceDe(Parametros.BASE_CUADRILLA);
        boolean[] visitado = new boolean[grafo.cantidadVertices()];
        List<Integer> orden = AlgoritmosGrafo.dfs(grafo, base, visitado);
        System.out.println("DFS desde " + grafo.nombre(base) + ": " + AlgoritmosGrafo.nombres(grafo, orden));
        System.out.println("La red es conexa: " + (AlgoritmosGrafo.esConexo(grafo) ? "sí" : "no"));
        analizarTodosLosCortes();

        List<int[]> tramos = tramos();
        System.out.println("\nTramos:");
        for (int i = 0; i < tramos.size(); i++) {
            int[] t = tramos.get(i);
            System.out.printf("  %2d. %s - %s (%d km)%n", i, grafo.nombre(t[0]), grafo.nombre(t[1]), grafo.peso(t[0], t[1]));
        }
        int primero = consola.leerEntero("Tramo a cortar", 0, tramos.size() - 1, 0);
        int segundo = consola.leerEntero("Segundo tramo a cortar (-1 = ninguno)", -1, tramos.size() - 1, -1);
        int[] cortados = segundo == -1 || segundo == primero ? new int[] {primero} : new int[] {primero, segundo};
        simularCorte(tramos, cortados);
    }

    private List<int[]> tramos() {
        List<int[]> tramos = new ArrayList<>();
        for (int u = 0; u < grafo.cantidadVertices(); u++) {
            for (int w : grafo.vecinos(u)) {
                if (u < w) {
                    tramos.add(new int[] {u, w});
                }
            }
        }
        return tramos;
    }

    // corta los tramos indicados, informa qué localidades quedan aisladas y después los repone
    private void simularCorte(List<int[]> tramos, int[] cortados) {
        int[] km = new int[cortados.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cortados.length; i++) {
            int[] t = tramos.get(cortados[i]);
            km[i] = grafo.peso(t[0], t[1]);
            grafo.eliminarArista(t[0], t[1]);
            sb.append(sb.length() > 0 ? " y " : "").append(grafo.nombre(t[0])).append(" - ").append(grafo.nombre(t[1]));
        }
        System.out.println("\nCorte de " + sb + ":");
        informarAislamiento();
        for (int i = 0; i < cortados.length; i++) {
            int[] t = tramos.get(cortados[i]);
            grafo.agregarArista(t[0], t[1], km[i]);
        }
        System.out.println("(Los tramos se repusieron.)");
    }

    private void informarAislamiento() {
        int base = grafo.indiceDe(Parametros.BASE_CUADRILLA);
        boolean[] visitado = new boolean[grafo.cantidadVertices()];
        AlgoritmosGrafo.dfs(grafo, base, visitado);
        StringBuilder aisladas = new StringBuilder();
        for (int v = 0; v < visitado.length; v++) {
            if (!visitado[v]) {
                aisladas.append(aisladas.length() > 0 ? ", " : "").append(grafo.nombre(v));
            }
        }
        if (aisladas.length() == 0) {
            System.out.println("  La red sigue conexa: ninguna localidad queda aislada.");
        } else {
            System.out.println("  Quedan sin conexión con " + grafo.nombre(base) + ": " + aisladas);
        }
    }

    // prueba cortar cada tramo por separado (un tramo cuyo corte desconecta la red es un "puente")
    private void analizarTodosLosCortes() {
        List<String> puentes = new ArrayList<>();
        for (int[] t : tramos()) {
            int km = grafo.peso(t[0], t[1]);
            grafo.eliminarArista(t[0], t[1]);
            if (!AlgoritmosGrafo.esConexo(grafo)) {
                puentes.add(grafo.nombre(t[0]) + " - " + grafo.nombre(t[1]));
            }
            grafo.agregarArista(t[0], t[1], km);
        }
        System.out.println(puentes.isEmpty()
                ? "Se probó cortar cada uno de los " + grafo.cantidadAristas() + " tramos por separado: ninguno deja"
                        + " una localidad aislada (no hay puentes)."
                : "Tramos cuyo corte aísla alguna localidad: " + puentes);
    }

    private void dijkstra(int origen) {
        AlgoritmosGrafo.ResultadoDijkstra r = AlgoritmosGrafo.dijkstra(grafo, origen);
        System.out.println("Dijkstra desde " + grafo.nombre(origen) + ":");
        for (int v = 0; v < grafo.cantidadVertices(); v++) {
            System.out.printf("  %-17s %4d km   %s%n", grafo.nombre(v), r.distancia()[v],
                    AlgoritmosGrafo.nombres(grafo, AlgoritmosGrafo.camino(r, v)));
        }
    }

    private void matrizFloyd() {
        int n = grafo.cantidadVertices();
        int[][] d = floyd.distancia();
        StringBuilder sb = new StringBuilder("Floyd - distancias mínimas en km entre todos los pares\n     ");
        for (int v = 0; v < n; v++) {
            sb.append(String.format("%5s", "v" + v));
        }
        sb.append('\n');
        for (int u = 0; u < n; u++) {
            sb.append(String.format("%5s", "v" + u));
            for (int v = 0; v < n; v++) {
                sb.append(String.format("%5d", d[u][v]));
            }
            sb.append("   ").append(grafo.nombre(u)).append('\n');
        }
        System.out.print(sb);
    }

    // =====================================================================
    // M4: mediciones
    // =====================================================================

    private void mediciones(int lineas) throws IOException {
        LocalDate fecha = LocalDate.now();
        List<String> lista = new GeneradorDatos(Parametros.SEMILLA).generarMediciones(estaciones, lineas, fecha);
        String texto = String.join("\n", lista) + "\n";

        Path carpeta = Path.of(Parametros.CARPETA_DATOS);
        Files.createDirectories(carpeta);
        Path original = carpeta.resolve("mediciones_" + fecha + ".txt");
        Path comprimido = carpeta.resolve("mediciones_" + fecha + ".huf");
        Path recuperado = carpeta.resolve("mediciones_" + fecha + "_recuperado.txt");
        Files.writeString(original, texto, StandardCharsets.UTF_8);

        System.out.printf("Archivo de mediciones: %s (%,d líneas). Primeras líneas:%n", original, lineas);
        for (int i = 0; i < Math.min(3, lista.size()); i++) {
            System.out.println("  " + lista.get(i));
        }
        System.out.println();
        System.out.print(Huffman.analizar(texto, false));

        ArchivoHuffman.comprimir(texto, comprimido);
        String descomprimido = ArchivoHuffman.descomprimir(comprimido);
        Files.writeString(recuperado, descomprimido, StandardCharsets.UTF_8);
        long bytesOriginal = Files.size(original);
        long bytesComprimido = Files.size(comprimido);
        System.out.printf("%nArchivos reales: %s = %,d bytes - %s = %,d bytes (%.2f%%, incluye la tabla de frecuencias)%n",
                original.getFileName(), bytesOriginal, comprimido.getFileName(), bytesComprimido,
                100.0 * bytesComprimido / bytesOriginal);
        System.out.println("Descomprimido desde el .huf e idéntico al original: "
                + (descomprimido.equals(texto) ? "sí" : "NO") + " (" + recuperado.getFileName() + ")");
    }

    // =====================================================================
    // M6: experimentos
    // =====================================================================

    private void experimentos() throws IOException {
        System.out.println("Corriendo los experimentos (tarda unos segundos)...");
        String informe = InformeRendimiento.ejecutar(localidades());
        System.out.println(informe);
        System.out.println("Guardado en " + InformeRendimiento.guardar(informe));
    }

    // =====================================================================
    // Demos paso a paso
    // =====================================================================

    private void menuDemos() {
        while (true) {
            System.out.println("\n---------- DEMOS PASO A PASO ----------");
            System.out.println(" 1. AVL paso a paso (ver rotaciones)");
            System.out.println(" 2. ABB paso a paso (comparar con el AVL)");
            System.out.println(" 3. Tabla hash chica: lineal contra cuadrática");
            System.out.println(" 4. Huffman de una palabra");
            System.out.println(" 5. Josephus con N y M a elección");
            System.out.println(" 0. Volver");
            int opcion = consola.leerEntero("Opción", 0, 5, 0);
            switch (opcion) {
                case 0:
                    return;
                case 1:
                case 2:
                    trazaArbol(opcion == 1, leerClaves("Claves separadas por espacios", "10 20 30 25 28"));
                    break;
                case 3:
                    int tamanio = consola.leerEntero("Tamaño de la tabla", 5, 101, 11);
                    demoHash(tamanio, leerClaves("Números de estación separados por espacios", clavesQueChocan(tamanio)));
                    break;
                case 4:
                    String palabra = consola.leerTexto("Palabra o frase [WWXWYZWX]: ");
                    System.out.print(Huffman.analizar(palabra.isEmpty() ? "WWXWYZWX" : palabra, true));
                    break;
                case 5:
                    josephus(consola.leerEntero("N", 2, 100, 5), consola.leerEntero("M", 0, 100, 1));
                    break;
                default:
                    break;
            }
            consola.pausa();
        }
    }

    // lee números separados por espacios; lo que no sea un número entre 0 y 9999 se ignora
    private int[] leerClaves(String mensaje, String porDefecto) {
        String texto = consola.leerTexto(mensaje + " [" + porDefecto + "]: ");
        int[] claves = convertirClaves(texto);
        if (claves.length == 0) {
            if (!texto.isEmpty()) {
                System.out.println("  No se ingresó ningún número entre 0 y 9999: se usan " + porDefecto);
            }
            claves = convertirClaves(porDefecto);
        }
        return claves;
    }

    private static int[] convertirClaves(String texto) {
        List<Integer> claves = new ArrayList<>();
        for (String parte : texto.trim().split("[\\s,]+")) {
            try {
                int n = Integer.parseInt(parte);
                if (n >= 0 && n <= 9999) {
                    claves.add(n);
                }
            } catch (NumberFormatException e) {
                // se ignora
            }
        }
        return claves.stream().mapToInt(Integer::intValue).toArray();
    }

    private static Estacion estacionDemo(int numero) {
        return new Estacion(numero, "Demo", "Córdoba", "METEOROLOGICA", 50, 0);
    }

    private void trazaArbol(boolean avl, int[] claves) {
        AVL arbolAvl = new AVL();
        ABB arbolAbb = new ABB();
        arbolAvl.activarTraza();
        for (int clave : claves) {
            System.out.println("Insertar " + clave + ":");
            if (avl) {
                arbolAvl.insertar(estacionDemo(clave));
                String traza = arbolAvl.tomarTraza();
                System.out.print(traza.isEmpty() ? "    sin rotaciones\n" : traza);
                System.out.print(arbolAvl.dibujar());
            } else {
                arbolAbb.insertar(estacionDemo(clave));
                System.out.print(arbolAbb.dibujar());
            }
            System.out.println();
        }
        if (avl) {
            System.out.printf("Altura %d, %d nodos, %d hojas.%n", arbolAvl.altura(), arbolAvl.cantidadNodos(),
                    arbolAvl.cantidadHojas());
        } else {
            System.out.printf("Altura %d, %d nodos, %d hojas.%n", arbolAbb.altura(), arbolAbb.cantidadNodos(),
                    arbolAbb.cantidadHojas());
        }
    }

    // cuatro números que caen en la misma posición y dos en la siguiente
    private static String clavesQueChocan(int tamanio) {
        TablaHash tabla = new TablaHash(tamanio, TablaHash.Exploracion.LINEAL);
        int objetivo = tabla.hash(Estacion.codigoDe(0));
        StringBuilder mismas = new StringBuilder();
        StringBuilder siguientes = new StringBuilder();
        int enMismas = 0;
        int enSiguientes = 0;
        for (int n = 0; n < 10000 && (enMismas < 4 || enSiguientes < 2); n++) {
            int h = tabla.hash(Estacion.codigoDe(n));
            if (h == objetivo && enMismas < 4) {
                mismas.append(n).append(' ');
                enMismas++;
            } else if (h == (objetivo + 1) % tabla.tamanio() && enSiguientes < 2) {
                siguientes.append(n).append(' ');
                enSiguientes++;
            }
        }
        return (mismas.toString() + siguientes).trim();
    }

    private void demoHash(int tamanio, int[] numeros) {
        for (TablaHash.Exploracion exploracion : TablaHash.Exploracion.values()) {
            // umbral 0,99 para que no haga rehashing y se vean las colisiones en la tabla chica
            TablaHash tabla = new TablaHash(tamanio, exploracion, 0.99, TablaHash.FuncionHash.HORNER_MEZCLADO);
            tabla.activarTraza();
            System.out.println("Exploración " + (exploracion == TablaHash.Exploracion.LINEAL ? "lineal" : "cuadrática")
                    + ", tamaño " + tabla.tamanio() + ":");
            for (int n : numeros) {
                tabla.insertar(estacionDemo(n));
                System.out.println("  insertar " + Estacion.codigoDe(n) + ": " + tabla.tomarTraza());
            }
            System.out.print(tabla.mostrar());
            System.out.println("  Colisiones: " + tabla.getColisiones() + "\n");
        }
    }

    private void josephus(int n, int m) {
        System.out.println("Lista + iterador : " + java.util.Arrays.toString(JosephusSimple.ordenDeSalida(n, m)));
        System.out.println("Árbol con rangos : " + java.util.Arrays.toString(JosephusArbol.ordenDeSalida(n, m)));
        System.out.println("(los primeros N-1 son los eliminados en orden; el último es el que queda)");
    }
}
