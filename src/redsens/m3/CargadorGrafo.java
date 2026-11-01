package redsens.m3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// carga la red vial desde un archivo de texto con líneas "localidad;localidad;km"
public final class CargadorGrafo {

    // las dos representaciones del mismo grafo
    public record RedVial(GrafoMatriz matriz, GrafoLista lista) {
    }

    private CargadorGrafo() {
    }

    public static RedVial cargar(Path archivo) throws IOException {
        List<String[]> tramos = new ArrayList<>();
        List<String> nombres = new ArrayList<>();
        int numeroLinea = 0;
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            numeroLinea++;
            linea = linea.strip();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            String[] partes = linea.split(";");
            if (partes.length != 3) {
                throw new IOException("Línea " + numeroLinea + " mal formada: " + linea);
            }
            for (int i = 0; i < 2; i++) {
                partes[i] = partes[i].strip();
                if (!nombres.contains(partes[i])) {
                    nombres.add(partes[i]);
                }
            }
            tramos.add(partes);
        }

        String[] vertices = nombres.toArray(new String[0]);
        GrafoMatriz matriz = new GrafoMatriz(vertices);
        GrafoLista lista = new GrafoLista(vertices);
        for (String[] t : tramos) {
            int km = Integer.parseInt(t[2].strip());
            int u = matriz.indiceDe(t[0]);
            int v = matriz.indiceDe(t[1]);
            matriz.agregarArista(u, v, km);
            lista.agregarArista(u, v, km);
        }
        return new RedVial(matriz, lista);
    }
}
