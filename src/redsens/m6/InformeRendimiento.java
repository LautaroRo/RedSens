package redsens.m6;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import redsens.modelo.Estacion;
import redsens.modelo.Parametros;
import redsens.m1.ExperimentoHash;
import redsens.m2.ExperimentoArboles;
import redsens.m4.Huffman;
import redsens.m5.Guardias;

// M6 - Corre la batería de experimentos de M1, M2 y M5 (más las verificaciones) y la guarda en un archivo
public final class InformeRendimiento {

    private InformeRendimiento() {
    }

    public static String ejecutar(String[] localidades) {
        StringBuilder sb = new StringBuilder();
        sb.append("INFORME DE RENDIMIENTO - RedSens\n");
        sb.append(Parametros.resumen()).append('\n');
        sb.append("Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append(" - Java ").append(System.getProperty("java.version")).append("\n\n");

        Estacion[] estaciones = new GeneradorDatos(Parametros.SEMILLA)
                .generarEstaciones(GeneradorDatos.MAX_ESTACIONES, localidades);

        titulo(sb, "M6 - Generador de números aleatorios");
        sb.append(PruebasAleatorios.informe(Parametros.SEMILLA));

        titulo(sb, "M1 - Tabla hash");
        sb.append(ExperimentoHash.ejecutar(estaciones, Parametros.TAMANIO_INICIAL_TABLA));

        titulo(sb, "M2 - ABB contra AVL");
        sb.append(ExperimentoArboles.ejecutar(estaciones));

        titulo(sb, "M4 - Caso de verificación de Huffman: WWXWYZWX");
        sb.append(Huffman.analizar("WWXWYZWX", false));

        titulo(sb, "M5 - Josephus");
        sb.append(Guardias.casoVerificacion()).append('\n');
        sb.append(Guardias.experimento(Parametros.M_JOSEPHUS));
        return sb.toString();
    }

    private static void titulo(StringBuilder sb, String texto) {
        sb.append('\n').append("=".repeat(texto.length())).append('\n').append(texto).append('\n')
                .append("=".repeat(texto.length())).append('\n');
    }

    public static Path guardar(String informe) throws IOException {
        Path carpeta = Path.of(Parametros.CARPETA_RESULTADOS);
        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve("informe_rendimiento_grupo" + Parametros.GRUPO + ".txt");
        Files.writeString(archivo, informe, StandardCharsets.UTF_8);
        return archivo;
    }
}
