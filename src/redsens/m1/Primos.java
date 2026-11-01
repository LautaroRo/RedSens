package redsens.m1;

// utilidades de números primos para el tamaño de las tablas hash
public final class Primos {

    private Primos() {
    }

    public static boolean esPrimo(int n) {
        if (n < 2) {
            return false;
        }
        if (n % 2 == 0) {
            return n == 2;
        }
        for (int d = 3; (long) d * d <= n; d += 2) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }

    // el primer primo mayor o igual que n
    public static int siguientePrimo(int n) {
        int candidato = Math.max(n, 2);
        while (!esPrimo(candidato)) {
            candidato++;
        }
        return candidato;
    }
}
