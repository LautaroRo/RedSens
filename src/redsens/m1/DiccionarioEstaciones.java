package redsens.m1;

import redsens.modelo.Estacion;

// operaciones comunes de las tablas hash del M1, para poder compararlas en el mismo experimento
public interface DiccionarioEstaciones {

    // devuelve false si el código ya estaba
    boolean insertar(Estacion estacion);

    // devuelve null si el código no está
    Estacion buscar(String codigo);

    boolean eliminar(String codigo);

    // celdas o nodos examinados por la última operación
    int getSondeosUltimaOperacion();

    long getColisiones();

    int getRehashings();

    int tamanio();

    int cantidad();

    // factor de carga λ = elementos / tamaño de la tabla
    double factorCarga();

    String descripcion();
}
