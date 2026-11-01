package redsens.modelo;

// una estación de medición de la red (modelo de datos mínimo, sección 1.1)
public class Estacion {

    // menos de este porcentaje de batería, la estación es crítica
    public static final int UMBRAL_CRITICO = 20;

    private final String codigo;
    private final int numero;
    private final String nombre;
    private final String localidad;
    private final String tipo;
    private int bateria;
    private int diasSinVisita;

    public Estacion(int numero, String nombre, String localidad, String tipo, int bateria, int diasSinVisita) {
        if (numero < 0 || numero > 9999) {
            throw new IllegalArgumentException("El número de estación va de 0 a 9999: " + numero);
        }
        this.numero = numero;
        this.codigo = codigoDe(numero);
        this.nombre = nombre;
        this.localidad = localidad;
        this.tipo = tipo;
        this.bateria = bateria;
        this.diasSinVisita = diasSinVisita;
    }

    // arma el código con el formato CBA-NNNN a partir del número
    public static String codigoDe(int numero) {
        return String.format("CBA-%04d", numero);
    }

    public boolean esCritica() {
        return bateria < UMBRAL_CRITICO;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLocalidad() {
        return localidad;
    }

    public String getTipo() {
        return tipo;
    }

    public int getBateria() {
        return bateria;
    }

    public int getDiasSinVisita() {
        return diasSinVisita;
    }

    @Override
    public String toString() {
        return String.format("%s  %-34s %-17s %-14s bat %3d%%  %3d días sin visita",
                codigo, nombre, localidad, tipo, bateria, diasSinVisita);
    }
}
