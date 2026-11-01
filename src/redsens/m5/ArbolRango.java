package redsens.m5;

// árbol binario de búsqueda donde cada nodo guarda el tamaño de su subárbol
class ArbolRango {

    private static final class Nodo {
        int valor;
        int tamanio;
        Nodo izq;
        Nodo der;
    }

    private Nodo raiz;
    private int eliminado;

    // arma el árbol perfectamente balanceado con los valores 1..n (altura ≈ log2 n)
    ArbolRango(int n) {
        raiz = construir(1, n);
    }

    private static Nodo construir(int desde, int hasta) {
        if (desde > hasta) {
            return null;
        }
        int medio = (desde + hasta) >>> 1;
        Nodo n = new Nodo();
        n.valor = medio;
        n.izq = construir(desde, medio - 1);
        n.der = construir(medio + 1, hasta);
        n.tamanio = hasta - desde + 1;
        return n;
    }

    private static int tamanio(Nodo n) {
        return n == null ? 0 : n.tamanio;
    }

    // el k-ésimo menor (k desde 1)
    int kesimo(int k) {
        return kesimo(raiz, k).valor;
    }

    private static Nodo kesimo(Nodo n, int k) {
        while (true) {
            int izquierda = tamanio(n.izq);
            if (k <= izquierda) {
                n = n.izq;
            } else if (k == izquierda + 1) {
                return n;
            } else {
                k -= izquierda + 1;
                n = n.der;
            }
        }
    }

    // quita el k-ésimo y lo devuelve
    int eliminarKesimo(int k) {
        raiz = eliminarKesimo(raiz, k);
        return eliminado;
    }

    private Nodo eliminarKesimo(Nodo n, int k) {
        int izquierda = tamanio(n.izq);
        if (k <= izquierda) {
            n.izq = eliminarKesimo(n.izq, k);
        } else if (k > izquierda + 1) {
            n.der = eliminarKesimo(n.der, k - izquierda - 1);
        } else {
            int valor = n.valor;
            if (n.izq == null) {
                eliminado = valor;
                return n.der;
            }
            if (n.der == null) {
                eliminado = valor;
                return n.izq;
            }
            // Dos hijos: toma el valor del menor del subárbol derecho y se borra ese nodo.
            n.valor = kesimo(n.der, 1).valor;
            n.der = eliminarKesimo(n.der, 1);
            eliminado = valor;
        }
        n.tamanio--;
        return n;
    }
}
