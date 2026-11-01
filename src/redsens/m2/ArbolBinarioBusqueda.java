package redsens.m2;

import java.util.List;

import redsens.modelo.Estacion;

// M2 - Lo que comparten el ABB y el AVL
public abstract class ArbolBinarioBusqueda {

    protected Nodo raiz;

    public abstract boolean insertar(Estacion estacion);

    public abstract boolean eliminar(int clave);

    public abstract String nombre();

    // búsqueda iterativa
    public Estacion buscar(int clave) {
        Nodo actual = raiz;
        while (actual != null) {
            if (clave < actual.clave) {
                actual = actual.izq;
            } else if (clave > actual.clave) {
                actual = actual.der;
            } else {
                return actual.dato;
            }
        }
        return null;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public void vaciar() {
        raiz = null;
    }

    // ---------- Recorridos ----------

    // izquierdo, raíz, derecho
    public void inorden(List<Estacion> salida) {
        inorden(raiz, salida);
    }

    private void inorden(Nodo n, List<Estacion> salida) {
        if (n == null) {
            return;                                 // caso base: subárbol vacío
        }
        inorden(n.izq, salida);
        salida.add(n.dato);
        inorden(n.der, salida);
    }

    // raíz, izquierdo, derecho
    public void preorden(List<Estacion> salida) {
        preorden(raiz, salida);
    }

    private void preorden(Nodo n, List<Estacion> salida) {
        if (n == null) {
            return;                                 // caso base
        }
        salida.add(n.dato);
        preorden(n.izq, salida);
        preorden(n.der, salida);
    }

    // izquierdo, derecho, raíz
    public void postorden(List<Estacion> salida) {
        postorden(raiz, salida);
    }

    private void postorden(Nodo n, List<Estacion> salida) {
        if (n == null) {
            return;                                 // caso base
        }
        postorden(n.izq, salida);
        postorden(n.der, salida);
        salida.add(n.dato);
    }

    // ---------- Métodos recursivos pedidos ----------

    // altura en aristas
    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo n) {
        if (n == null) {
            return -1;                              // caso base: árbol vacío
        }
        return 1 + Math.max(altura(n.izq), altura(n.der));
    }

    public int cantidadNodos() {
        return cantidadNodos(raiz);
    }

    private int cantidadNodos(Nodo n) {
        if (n == null) {
            return 0;                               // caso base: árbol vacío
        }
        return 1 + cantidadNodos(n.izq) + cantidadNodos(n.der);
    }

    public int cantidadHojas() {
        return cantidadHojas(raiz);
    }

    private int cantidadHojas(Nodo n) {
        if (n == null) {
            return 0;                               // caso base 1: árbol vacío
        }
        if (n.izq == null && n.der == null) {
            return 1;                               // caso base 2: es una hoja
        }
        return cantidadHojas(n.izq) + cantidadHojas(n.der);
    }

    // ---------- Consultas ----------

    // estaciones con número entre a y b, en orden
    public void rango(int a, int b, List<Estacion> salida) {
        rango(raiz, a, b, salida);
    }

    private void rango(Nodo n, int a, int b, List<Estacion> salida) {
        if (n == null) {
            return;                                 // caso base
        }
        if (a < n.clave) {
            rango(n.izq, a, b, salida);
        }
        if (a <= n.clave && n.clave <= b) {
            salida.add(n.dato);
        }
        if (n.clave < b) {
            rango(n.der, a, b, salida);
        }
    }

    // reporte de críticas (batería < 20), ordenadas por número
    public void criticas(List<Estacion> salida) {
        criticas(raiz, salida);
    }

    private void criticas(Nodo n, List<Estacion> salida) {
        if (n == null) {
            return;                                 // caso base
        }
        criticas(n.izq, salida);
        if (n.dato.esCritica()) {
            salida.add(n.dato);
        }
        criticas(n.der, salida);
    }

    // ---------- Dibujo para las demos ----------

    // dibuja el árbol de costado (la raíz a la izquierda, el subárbol derecho arriba)
    public String dibujar() {
        if (raiz == null) {
            return "  (árbol vacío)\n";
        }
        StringBuilder sb = new StringBuilder();
        dibujar(raiz, "", true, true, sb);
        return sb.toString();
    }

    private void dibujar(Nodo n, String prefijo, boolean esDerecho, boolean esRaiz, StringBuilder sb) {
        if (n == null) {
            return;
        }
        dibujar(n.der, prefijo + (esRaiz ? "  " : esDerecho ? "        " : "|       "), true, false, sb);
        sb.append(prefijo).append(esRaiz ? "" : esDerecho ? "/-- " : "\\-- ").append(n.clave)
                .append(etiqueta(n)).append('\n');
        dibujar(n.izq, prefijo + (esRaiz ? "  " : esDerecho ? "|       " : "        "), false, false, sb);
    }

    // lo que se muestra al lado de cada clave en el dibujo
    protected String etiqueta(Nodo n) {
        return "";
    }
}
