package redsens.m2;

import redsens.modelo.Estacion;

// M2 - Arbol AVL. Despues de insertar o borrar se recalcula el factor de equilibrio volviendo
// hacia la raiz, y si algun nodo queda en +2 o -2 se rota (simple o doble).
public class AVL extends ArbolBinarioBusqueda {

    private boolean cambio;
    private int rotacionesSimples;
    private int rotacionesDobles;
    private StringBuilder traza;   // solo para la demo paso a paso

    @Override
    public boolean insertar(Estacion estacion) {
        cambio = false;
        raiz = insertar(raiz, estacion.getNumero(), estacion);
        return cambio;
    }

    private Nodo insertar(Nodo n, int clave, Estacion estacion) {
        if (n == null) {
            cambio = true;
            return new Nodo(clave, estacion);       // caso base: lugar vacío, nace una hoja (altura 0)
        }
        if (clave < n.clave) {
            n.izq = insertar(n.izq, clave, estacion);
        } else if (clave > n.clave) {
            n.der = insertar(n.der, clave, estacion);
        } else {
            return n;                               // clave repetida
        }
        return balancear(n);
    }

    @Override
    public boolean eliminar(int clave) {
        cambio = false;
        raiz = eliminar(raiz, clave);
        return cambio;
    }

    private Nodo eliminar(Nodo n, int clave) {
        if (n == null) {
            return null;
        }
        if (clave < n.clave) {
            n.izq = eliminar(n.izq, clave);
        } else if (clave > n.clave) {
            n.der = eliminar(n.der, clave);
        } else {
            cambio = true;
            if (n.izq == null || n.der == null) {
                return n.izq != null ? n.izq : n.der;   // casos 1 y 2: hoja o un solo hijo
            }
            Nodo sucesor = n.der;                       // caso 3: dos hijos, igual que en el ABB
            while (sucesor.izq != null) {
                sucesor = sucesor.izq;
            }
            n.clave = sucesor.clave;
            n.dato = sucesor.dato;
            n.der = eliminar(n.der, sucesor.clave);
        }
        return balancear(n);                            // a la vuelta se rebalancea cada ancestro
    }

    private static int h(Nodo n) {
        return n == null ? -1 : n.altura;
    }

    private static void actualizarAltura(Nodo n) {
        n.altura = 1 + Math.max(h(n.izq), h(n.der));
    }

    // factor de equilibrio = altura(izq) - altura(der)
    static int factorEquilibrio(Nodo n) {
        return h(n.izq) - h(n.der);
    }

    private Nodo balancear(Nodo n) {
        actualizarAltura(n);
        int fe = factorEquilibrio(n);
        if (fe > 1) {                                   // cargado a la izquierda
            if (factorEquilibrio(n.izq) >= 0) {
                registrar("rotación simple a derecha en " + n.clave + " (caso izquierda-izquierda)");
                rotacionesSimples++;
                return rotarDerecha(n);
            }
            registrar("rotación doble izquierda-derecha en " + n.clave);
            rotacionesDobles++;
            n.izq = rotarIzquierda(n.izq);
            return rotarDerecha(n);
        }
        if (fe < -1) {                                  // cargado a la derecha
            if (factorEquilibrio(n.der) <= 0) {
                registrar("rotación simple a izquierda en " + n.clave + " (caso derecha-derecha)");
                rotacionesSimples++;
                return rotarIzquierda(n);
            }
            registrar("rotación doble derecha-izquierda en " + n.clave);
            rotacionesDobles++;
            n.der = rotarDerecha(n.der);
            return rotarIzquierda(n);
        }
        return n;
    }

    //       k2            k1
    //      /  \          /  \
    //     k1   C   =>   A    k2
    //    /  \               /  \
    //   A    B             B    C
    private static Nodo rotarDerecha(Nodo k2) {
        Nodo k1 = k2.izq;
        k2.izq = k1.der;
        k1.der = k2;
        actualizarAltura(k2);
        actualizarAltura(k1);
        return k1;
    }

    // espejo de rotarDerecha
    private static Nodo rotarIzquierda(Nodo k1) {
        Nodo k2 = k1.der;
        k1.der = k2.izq;
        k2.izq = k1;
        actualizarAltura(k1);
        actualizarAltura(k2);
        return k2;
    }

    private void registrar(String texto) {
        if (traza != null) {
            traza.append("    ").append(texto).append('\n');
        }
    }

    public void activarTraza() {
        traza = new StringBuilder();
    }

    public String tomarTraza() {
        String t = traza == null ? "" : traza.toString();
        if (traza != null) {
            traza.setLength(0);
        }
        return t;
    }

    public int getRotacionesSimples() {
        return rotacionesSimples;
    }

    public int getRotacionesDobles() {
        return rotacionesDobles;
    }

    @Override
    protected String etiqueta(Nodo n) {
        return " (fe=" + factorEquilibrio(n) + ")";
    }

    @Override
    public String nombre() {
        return "AVL";
    }
}
