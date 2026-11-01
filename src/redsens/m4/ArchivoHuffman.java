package redsens.m4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// guarda y lee el archivo comprimido real (.huf), para poder transmitirlo
public final class ArchivoHuffman {

    private ArchivoHuffman() {
    }

    public static void comprimir(String texto, Path destino) throws IOException {
        int[] frecuencias = Huffman.contarFrecuencias(texto);
        Huffman.Nodo raiz = Huffman.construirArbolHuffman(frecuencias);
        Map<Character, String> tabla = Huffman.codificar(raiz);
        String bits = Huffman.comprimir(texto, tabla);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(destino)))) {
            out.writeInt(tabla.size());
            for (int c = 0; c < frecuencias.length; c++) {
                if (frecuencias[c] > 0) {
                    out.writeChar(c);
                    out.writeInt(frecuencias[c]);
                }
            }
            out.writeLong(bits.length());
            int acumulado = 0;
            int enByte = 0;
            for (int i = 0; i < bits.length(); i++) {
                acumulado = (acumulado << 1) | (bits.charAt(i) - '0');
                if (++enByte == 8) {
                    out.write(acumulado);
                    acumulado = 0;
                    enByte = 0;
                }
            }
            if (enByte > 0) {
                out.write(acumulado << (8 - enByte));   // el último byte se completa con ceros
            }
        }
    }

    public static String descomprimir(Path origen) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(origen)))) {
            int[] frecuencias = new int[Character.MAX_VALUE + 1];
            int simbolos = in.readInt();
            for (int i = 0; i < simbolos; i++) {
                char c = in.readChar();
                frecuencias[c] = in.readInt();
            }
            long cantidadBits = in.readLong();
            StringBuilder bits = new StringBuilder((int) cantidadBits);
            while (bits.length() < cantidadBits) {
                int b = in.read();
                for (int i = 7; i >= 0 && bits.length() < cantidadBits; i--) {
                    bits.append((b >> i) & 1);
                }
            }
            return Huffman.decodificar(bits.toString(), Huffman.construirArbolHuffman(frecuencias));
        }
    }
}
