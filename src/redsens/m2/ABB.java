package redsens.m2;

import redsens.modelo.Estacion;

// M2 - Árbol binario de búsqueda sin balanceo, con clave = número de estación
public class ABB extends ArbolBinarioBusqueda {

    private boolean eliminado;

    // insercion iterativa: con claves ordenadas el arbol queda como una lista y recursivo serian 10.000 llamadas
    @Override
    public boolean insertar(Estacion estacion) {
        int clave = estacion.getNumero();
        if (raiz == null) {
            raiz = new Nodo(clave, estacion);
            return true;
        }
        Nodo actual = raiz;
        while (true) {
            if (clave < actual.clave) {
                if (actual.izq == null) {
                    actual.izq = new Nodo(clave, estacion);
                    return true;
                }
                actual = actual.izq;
            } else if (clave > actual.clave) {
                if (actual.der == null) {
                    actual.der = new Nodo(clave, estacion);
                    return true;
                }
                actual = actual.der;
            } else {
                return false;   // clave repetida
            }
        }
    }

    @Override
    public boolean eliminar(int clave) {
        eliminado = false;
        raiz = eliminar(raiz, clave);
        return eliminado;
    }

    private Nodo eliminar(Nodo n, int clave) {
        if (n == null) {
            return null;                            // la clave no estaba
        }
        if (clave < n.clave) {
            n.izq = eliminar(n.izq, clave);
        } else if (clave > n.clave) {
            n.der = eliminar(n.der, clave);
        } else {
            eliminado = true;
            if (n.izq == null && n.der == null) {
                return null;                        // caso 1: hoja, se quita
            }
            if (n.izq == null) {
                return n.der;                       // caso 2: un solo hijo, el hijo ocupa su lugar
            }
            if (n.der == null) {
                return n.izq;
            }
            Nodo sucesor = n.der;                   // caso 3: dos hijos, se reemplaza por el
            while (sucesor.izq != null) {           // mínimo del subárbol derecho (el sucesor inorden)
                sucesor = sucesor.izq;
            }
            n.clave = sucesor.clave;
            n.dato = sucesor.dato;
            n.der = eliminar(n.der, sucesor.clave); // y se elimina ese mínimo, que tiene a lo sumo un hijo
        }
        return n;
    }

    @Override
    public String nombre() {
        return "ABB";
    }
}
