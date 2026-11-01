package redsens.m3;

// M3 - Grafo no dirigido y ponderado de localidades
public interface Grafo {

    // distancia "sin camino"
    int INFINITO = Integer.MAX_VALUE / 2;

    int cantidadVertices();

    int cantidadAristas();

    String nombre(int v);

    // índice del vértice con ese nombre, o -1
    int indiceDe(String nombre);

    void agregarArista(int u, int v, int km);

    boolean eliminarArista(int u, int v);

    // km del tramo directo u-v, 0 si u == v, INFINITO si no hay tramo
    int peso(int u, int v);

    // vecinos de v en orden creciente de índice
    int[] vecinos(int v);

    String nombreRepresentacion();

    String mostrar();
}
