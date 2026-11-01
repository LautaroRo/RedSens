# RedSens

Trabajo Práctico Integral de **Taller de Algoritmos y Estructura de Datos II** - Universidad Siglo 21 (2026).

El caso es el de DATA Investigaciones, la empresa de las lecturas de la materia: tienen una red de estaciones
de medición en Córdoba y hay que armarles un sistema de consola para manejarla. Lo hicimos en Java.

## Qué hace

El programa tiene un menú y seis módulos:

- **M1 - Registro:** tabla hash propia (hash cerrado) para buscar estaciones por código. Tiene exploración lineal y
  cuadrática, eliminación perezosa y rehashing. También hicimos la versión con encadenamiento (era opcional).
- **M2 - Índice ordenado:** ABB y AVL ordenados por número de estación. Listado en orden, búsqueda por rango y
  reporte de estaciones críticas (batería menor a 20).
- **M3 - Red vial:** grafo de las localidades cargado desde `datos/rutas.txt`. BFS, DFS, Dijkstra, Floyd y un plan de
  visitas para la cuadrilla con el criterio del vecino más cercano.
- **M4 - Transmisión:** compresión del archivo de mediciones con Huffman.
- **M5 - Guardias:** calendario de guardias con el problema de Josephus (con lista y con árbol).
- **M6 - Simulación:** genera los datos con `Random` y la semilla del grupo, y mide los tiempos de los experimentos.

No usamos `HashMap`, `TreeMap` ni librerías de grafos en M1, M2 y M3 porque la consigna no lo permitía.

## Cómo correrlo

Hace falta el JDK 17 o más nuevo.

Con IntelliJ: abrir la carpeta y ejecutar `redsens.Main` (la carpeta de trabajo tiene que ser la del proyecto,
porque lee `datos/rutas.txt`).

Desde PowerShell:

```powershell
.\compilar_y_ejecutar.ps1
```

O a mano:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out redsens.Main
```

Con `--experimentos` corre todos los experimentos y los guarda en `resultados/`.

## Carpetas

- `src/redsens/` - el código, un paquete por módulo (`m1` a `m6`) y el `Main` con el menú
- `datos/` - el grafo de rutas y el archivo de mediciones (original, comprimido y descomprimido)
- `resultados/` - lo que sale de los experimentos
- `docs/` - el informe técnico

## Parámetros del grupo

La semilla, el tamaño de la tabla, la localidad base y el M de Josephus dependen del número de grupo.
Están en `src/redsens/modelo/Parametros.java`, se cambia `GRUPO` y listo.

## Integrantes

| Integrante | Rol principal |
|---|---|
| Valentino | M6 Simulación y M5 Guardias |
| Bruno A | M1 Registro (tabla hash) |
| Nacho | M2 Índice ordenado (ABB y AVL) |
| Lautaro | M3 Red vial: representación, BFS y DFS |
| Bruno B | M3 Red vial: Dijkstra, Floyd y plan de visitas |
| Aitana | M4 Transmisión (Huffman) |
