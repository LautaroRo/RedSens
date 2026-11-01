package redsens.m4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// M4 - Algoritmo de Huffman con la estructura de la lectura de implementación
public final class Huffman {

    public static final class Nodo implements Comparable<Nodo> {
        final char simbolo;
        final int frecuencia;
        final Nodo izq;
        final Nodo der;
        final int orden;    // orden de creación: desempata frecuencias iguales para que el árbol sea siempre el mismo

        Nodo(char simbolo, int frecuencia, Nodo izq, Nodo der, int orden) {
            this.simbolo = simbolo;
            this.frecuencia = frecuencia;
            this.izq = izq;
            this.der = der;
            this.orden = orden;
        }

        boolean esHoja() {
            return izq == null && der == null;
        }

        @Override
        public int compareTo(Nodo otro) {
            if (frecuencia != otro.frecuencia) {
                return Integer.compare(frecuencia, otro.frecuencia);
            }
            return Integer.compare(orden, otro.orden);
        }
    }

    private Huffman() {
    }

    // frecuencia de cada carácter
    public static int[] contarFrecuencias(String texto) {
        int[] frecuencias = new int[Character.MAX_VALUE + 1];
        for (int i = 0; i < texto.length(); i++) {
            frecuencias[texto.charAt(i)]++;
        }
        return frecuencias;
    }

    // mete una hoja por símbolo en una cola de prioridad y repite
    public static Nodo construirArbolHuffman(int[] frecuencias) {
        PriorityQueue<Nodo> cola = new PriorityQueue<>();
        int orden = 0;
        for (int c = 0; c < frecuencias.length; c++) {
            if (frecuencias[c] > 0) {
                cola.add(new Nodo((char) c, frecuencias[c], null, null, orden++));
            }
        }
        if (cola.isEmpty()) {
            return null;
        }
        if (cola.size() == 1) {
            // Con un solo símbolo la raíz sería una hoja con código vacío: se le agrega un padre
            // para que el símbolo reciba el código "0".
            Nodo unico = cola.poll();
            return new Nodo('\0', unico.frecuencia, unico, null, orden);
        }
        while (cola.size() > 1) {
            Nodo menor = cola.poll();
            Nodo segundo = cola.poll();
            cola.add(new Nodo('\0', menor.frecuencia + segundo.frecuencia, menor, segundo, orden++));
        }
        return cola.poll();
    }

    // tabla de códigos
    public static HashMap<Character, String> codificar(Nodo raiz) {
        HashMap<Character, String> tabla = new HashMap<>();
        generarCodigos(raiz, "", tabla);
        return tabla;
    }

    private static void generarCodigos(Nodo n, String prefijo, Map<Character, String> tabla) {
        if (n == null) {
            return;
        }
        if (n.esHoja()) {
            tabla.put(n.simbolo, prefijo);          // caso base: llegó a un símbolo
            return;
        }
        generarCodigos(n.izq, prefijo + "0", tabla);
        generarCodigos(n.der, prefijo + "1", tabla);
    }

    // reemplaza cada carácter por su código
    public static String comprimir(String texto, Map<Character, String> tabla) {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            bits.append(tabla.get(texto.charAt(i)));
        }
        return bits.toString();
    }

    // baja por el árbol bit a bit desde la raíz; al llegar a una hoja emite su símbolo y vuelve a la raíz
    public static String decodificar(String bits, Nodo raiz) {
        StringBuilder texto = new StringBuilder();
        Nodo actual = raiz;
        for (int i = 0; i < bits.length(); i++) {
            actual = bits.charAt(i) == '0' ? actual.izq : actual.der;
            if (actual.esHoja()) {
                texto.append(actual.simbolo);
                actual = raiz;
            }
        }
        return texto.toString();
    }

    // verifica que ningún código sea prefijo de otro comparando todos los pares
    public static boolean esLibreDePrefijos(Map<Character, String> tabla) {
        List<String> codigos = new ArrayList<>(tabla.values());
        for (int i = 0; i < codigos.size(); i++) {
            for (int j = 0; j < codigos.size(); j++) {
                if (i != j && codigos.get(j).startsWith(codigos.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    // tabla de frecuencias y códigos, de más a menos frecuente
    public static String tablaDeCodigos(int[] frecuencias, Map<Character, String> tabla) {
        List<Character> simbolos = new ArrayList<>(tabla.keySet());
        simbolos.sort((a, b) -> frecuencias[b] != frecuencias[a]
                ? Integer.compare(frecuencias[b], frecuencias[a]) : Character.compare(a, b));
        StringBuilder sb = new StringBuilder(String.format("  %-9s %10s  %-20s %5s%n", "Símbolo", "Frecuencia",
                "Código", "Bits"));
        for (char c : simbolos) {
            String codigo = tabla.get(c);
            sb.append(String.format("  %-9s %10d  %-20s %5d%n", mostrarSimbolo(c), frecuencias[c], codigo,
                    codigo.length()));
        }
        return sb.toString();
    }

    public static String mostrarSimbolo(char c) {
        switch (c) {
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            case ' ':
                return "espacio";
            default:
                return "'" + c + "'";
        }
    }

    // dibuja el árbol de costado; las hojas muestran símbolo:frecuencia y los internos solo la frecuencia
    public static String dibujar(Nodo raiz) {
        StringBuilder sb = new StringBuilder();
        dibujar(raiz, "", "", sb);
        return sb.toString();
    }

    private static void dibujar(Nodo n, String prefijo, String rama, StringBuilder sb) {
        if (n == null) {
            return;
        }
        dibujar(n.der, prefijo + "      ", "1/ ", sb);
        sb.append(prefijo).append(rama)
                .append(n.esHoja() ? mostrarSimbolo(n.simbolo) + ":" + n.frecuencia : "(" + n.frecuencia + ")")
                .append('\n');
        dibujar(n.izq, prefijo + "      ", "0\\ ", sb);
    }

    // resumen completo de comprimir un texto
    public static String analizar(String texto, boolean mostrarArbol) {
        int[] frecuencias = contarFrecuencias(texto);
        Nodo raiz = construirArbolHuffman(frecuencias);
        HashMap<Character, String> tabla = codificar(raiz);
        String bits = comprimir(texto, tabla);
        String recuperado = decodificar(bits, raiz);

        long original = 8L * texto.length();
        StringBuilder sb = new StringBuilder();
        sb.append(tablaDeCodigos(frecuencias, tabla));
        if (mostrarArbol) {
            sb.append("\nÁrbol de Huffman (0 = izquierda, abajo; 1 = derecha, arriba):\n").append(dibujar(raiz));
            sb.append("\nTexto codificado: ").append(bits).append('\n');
        }
        sb.append(String.format("%nSímbolos distintos: %d%n", tabla.size()));
        sb.append("Ningún código es prefijo de otro: ").append(esLibreDePrefijos(tabla) ? "sí" : "NO").append('\n');
        sb.append(String.format("Tamaño original (8 bits por carácter): %,d bits%n", original));
        sb.append(String.format("Tamaño comprimido:                     %,d bits%n", bits.length()));
        sb.append(String.format("Bits promedio por carácter:            %.3f%n", (double) bits.length() / texto.length()));
        sb.append(String.format("Tasa de compresión: %.2f%% del original (ahorro del %.2f%%)%n",
                100.0 * bits.length() / original, 100.0 - 100.0 * bits.length() / original));
        sb.append("Decodificado idéntico al original: ").append(recuperado.equals(texto) ? "sí" : "NO").append('\n');
        return sb.toString();
    }
}
