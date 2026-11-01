package redsens.m2;

import redsens.modelo.Estacion;

// nodo de los árboles del M2
class Nodo {
    int clave;
    Estacion dato;
    Nodo izq;
    Nodo der;
    int altura;

    Nodo(int clave, Estacion dato) {
        this.clave = clave;
        this.dato = dato;
    }
}
