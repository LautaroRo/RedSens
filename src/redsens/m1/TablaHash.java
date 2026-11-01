package redsens.m1;

import redsens.modelo.Estacion;

// M1 - Tabla hash con direccionamiento abierto (hash cerrado).
// Se elige exploracion lineal o cuadratica al crearla. La baja es perezosa (se marca la celda)
// y cuando se pasa del umbral se hace rehashing al primo siguiente del doble.
public class TablaHash implements DiccionarioEstaciones {

    public enum Exploracion { LINEAL, CUADRATICA }

    // usamos HORNER_MEZCLADO. Las otras dos las dejamos para comparar en el experimento
    // (HORNER fue nuestra primera version)
    public enum FuncionHash { HORNER_MEZCLADO, HORNER, SUMA_CARACTERES }

    // lineal: con mas de 0.5 el agrupamiento primario dispara los sondeos (8.5 en una busqueda fallida con 0.75)
    public static final double UMBRAL_LINEAL = 0.5;

    // cuadratica: con tamaño primo y lambda <= 0.5 siempre encuentra lugar
    public static final double UMBRAL_CUADRATICA = 0.5;

    private static final class Celda {
        final Estacion estacion;
        boolean activa = true;

        Celda(Estacion estacion) {
            this.estacion = estacion;
        }
    }

    private final Exploracion exploracion;
    private final FuncionHash funcionHash;
    private final double umbral;

    private Celda[] celdas;
    private int ocupadas;   // activas + borradas: las borradas también alargan las búsquedas
    private int activas;

    private long colisiones;
    private int rehashings;
    private int sondeosUltimaOperacion;
    private StringBuilder traza;   // solo para las demos con tablas chicas

    public TablaHash(int tamanioInicial, Exploracion exploracion) {
        this(tamanioInicial, exploracion,
                exploracion == Exploracion.LINEAL ? UMBRAL_LINEAL : UMBRAL_CUADRATICA, FuncionHash.HORNER_MEZCLADO);
    }

    public TablaHash(int tamanioInicial, Exploracion exploracion, double umbral, FuncionHash funcionHash) {
        this.celdas = new Celda[Primos.siguientePrimo(tamanioInicial)];
        this.exploracion = exploracion;
        this.umbral = umbral;
        this.funcionHash = funcionHash;
    }

    // Funcion hash: Horner en base 31 (asi CBA-1234 y CBA-4321 dan distinto) y despues mezclamos los bits.
    // Sin la mezcla, CBA-0347 y CBA-0348 caian en celdas vecinas y con la lineal se armaban bloques.
    // floorMod porque si el int desborda h puede quedar negativo.
    public int hash(String clave) {
        int h = 0;
        if (funcionHash == FuncionHash.SUMA_CARACTERES) {
            for (int i = 0; i < clave.length(); i++) {
                h += clave.charAt(i);
            }
            return Math.floorMod(h, celdas.length);
        }
        for (int i = 0; i < clave.length(); i++) {
            h = 31 * h + clave.charAt(i);
        }
        if (funcionHash == FuncionHash.HORNER_MEZCLADO) {
            h ^= h >>> 16;
            h *= 0x45d9f3b;
            h ^= h >>> 16;
        }
        return Math.floorMod(h, celdas.length);
    }

    // recorre la secuencia de sondeo hasta encontrar la clave (activa o borrada) o una celda vacía
    private int buscarPosicion(String clave, boolean cuentaColisiones) {
        int m = celdas.length;
        int pos = hash(clave);
        int intento = 0;
        if (traza != null) {
            traza.append("h(").append(clave).append(") = ").append(pos).append(" -> sondea:");
        }
        while (true) {
            if (traza != null) {
                traza.append(' ').append(pos);
            }
            Celda c = celdas[pos];
            if (c == null || c.estacion.getCodigo().equals(clave)) {
                break;
            }
            if (cuentaColisiones) {
                colisiones++;
            }
            intento++;
            if (intento >= m) {
                sondeosUltimaOperacion = intento;
                return -1;
            }
            if (exploracion == Exploracion.LINEAL) {
                pos = pos + 1;                  // h, h+1, h+2, ...
            } else {
                pos = pos + 2 * intento - 1;    // h, h+1, h+4, h+9, ...: i² - (i-1)² = 2i - 1
            }
            pos %= m;
        }
        sondeosUltimaOperacion = intento + 1;
        return pos;
    }

    @Override
    public boolean insertar(Estacion estacion) {
        int pos = buscarPosicion(estacion.getCodigo(), true);
        if (pos == -1) {
            rehash();   // la cuadrática no encontró lugar: se agranda la tabla y se reintenta
            return insertar(estacion);
        }
        Celda c = celdas[pos];
        if (c != null && c.activa) {
            return false;                           // código repetido
        }
        celdas[pos] = new Celda(estacion);
        if (c == null) {
            ocupadas++;                             // si estaba borrada, la celda ya contaba como ocupada
        }
        activas++;
        if (ocupadas > umbral * celdas.length) {
            rehash();
        }
        return true;
    }

    @Override
    public Estacion buscar(String codigo) {
        int pos = buscarPosicion(codigo, false);
        if (pos == -1 || celdas[pos] == null || !celdas[pos].activa) {
            return null;
        }
        return celdas[pos].estacion;
    }

    // eliminación perezosa
    @Override
    public boolean eliminar(String codigo) {
        int pos = buscarPosicion(codigo, false);
        if (pos == -1 || celdas[pos] == null || !celdas[pos].activa) {
            return false;
        }
        celdas[pos].activa = false;
        activas--;
        return true;
    }

    // duplica el tamaño (al siguiente primo) y reinserta solo las activas
    private void rehash() {
        Celda[] anteriores = celdas;
        celdas = new Celda[Primos.siguientePrimo(2 * anteriores.length)];
        ocupadas = 0;
        activas = 0;
        rehashings++;
        StringBuilder trazaGuardada = traza;
        traza = null;
        for (Celda c : anteriores) {
            if (c != null && c.activa) {
                int pos = buscarPosicion(c.estacion.getCodigo(), false);
                celdas[pos] = new Celda(c.estacion);
                ocupadas++;
                activas++;
            }
        }
        traza = trazaGuardada;
        if (traza != null) {
            traza.append(" [rehash a ").append(celdas.length).append(']');
        }
    }

    public void activarTraza() {
        traza = new StringBuilder();
    }

    // devuelve lo que pasó en la última operación y empieza una traza nueva
    public String tomarTraza() {
        String t = traza == null ? "" : traza.toString();
        if (traza != null) {
            traza.setLength(0);
        }
        return t;
    }

    // dibuja todas las celdas
    public String mostrar() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < celdas.length; i++) {
            Celda c = celdas[i];
            String contenido = c == null ? "-" : c.activa ? c.estacion.getCodigo() : "(borrada " + c.estacion.getCodigo() + ")";
            sb.append(String.format("  [%2d] %s%n", i, contenido));
        }
        return sb.toString();
    }

    @Override
    public int getSondeosUltimaOperacion() {
        return sondeosUltimaOperacion;
    }

    @Override
    public long getColisiones() {
        return colisiones;
    }

    @Override
    public int getRehashings() {
        return rehashings;
    }

    @Override
    public int tamanio() {
        return celdas.length;
    }

    @Override
    public int cantidad() {
        return activas;
    }

    public int celdasOcupadas() {
        return ocupadas;
    }

    @Override
    public double factorCarga() {
        return (double) activas / celdas.length;
    }

    public Exploracion getExploracion() {
        return exploracion;
    }

    @Override
    public String descripcion() {
        String funcion = funcionHash == FuncionHash.HORNER_MEZCLADO ? ""
                : funcionHash == FuncionHash.HORNER ? " [Horner sin mezcla]" : " [suma de caracteres]";
        return String.format("%s, umbral %.2f%s", exploracion == Exploracion.LINEAL ? "Lineal" : "Cuadrática", umbral,
                funcion);
    }
}
