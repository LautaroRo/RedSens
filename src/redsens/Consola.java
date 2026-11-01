package redsens;

import java.util.Scanner;

// lectura de datos por teclado con valores por defecto y validación
final class Consola {

    private final Scanner scanner = new Scanner(System.in);

    String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    // lee un entero entre min y max; con Enter vacío devuelve el valor por defecto
    int leerEntero(String mensaje, int min, int max, int porDefecto) {
        while (true) {
            String texto = leerTexto(String.format("%s [%d]: ", mensaje, porDefecto));
            if (texto.isEmpty()) {
                return porDefecto;
            }
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // se vuelve a pedir
            }
            System.out.printf("  Ingresá un número entre %d y %d.%n", min, max);
        }
    }

    void pausa() {
        leerTexto("\n(Enter para seguir) ");
    }
}
