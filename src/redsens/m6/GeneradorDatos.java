package redsens.m6;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import redsens.modelo.Estacion;

// M6 - Genera estaciones y mediciones simuladas con java.util.Random y la semilla del grupo
public class GeneradorDatos {

    public static final String[] TIPOS = {"PLUVIOMETRICA", "METEOROLOGICA", "HIDROMETRICA"};
    private static final String[] PARAJES = {"Norte", "Sur", "Este", "Oeste", "Centro",
        "Embalse", "Cerro", "Arroyo", "Ruta", "Parque"};

    // hay 10.000 códigos posibles
    public static final int MAX_ESTACIONES = 10000;

    private final Random random;

    public GeneradorDatos(long semilla) {
        this.random = new Random(semilla);
    }

    // genera n estaciones con números distintos, en orden aleatorio
    public Estacion[] generarEstaciones(int n, String[] localidades) {
        if (n < 1 || n > MAX_ESTACIONES) {
            throw new IllegalArgumentException("Se pueden generar entre 1 y " + MAX_ESTACIONES + " estaciones");
        }
        int[] numeros = new int[MAX_ESTACIONES];
        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = i;
        }
        for (int i = 0; i < n; i++) {
            int j = i + random.nextInt(MAX_ESTACIONES - i);
            int aux = numeros[i];
            numeros[i] = numeros[j];
            numeros[j] = aux;
        }

        Estacion[] estaciones = new Estacion[n];
        for (int i = 0; i < n; i++) {
            String localidad = localidades[random.nextInt(localidades.length)];
            String nombre = "Estación " + PARAJES[random.nextInt(PARAJES.length)] + " " + localidad;
            String tipo = TIPOS[random.nextInt(TIPOS.length)];
            int bateria = random.nextInt(101);          // uniforme en 0..100
            int diasSinVisita = random.nextInt(181);    // hasta medio año
            estaciones[i] = new Estacion(numeros[i], nombre, localidad, tipo, bateria, diasSinVisita);
        }
        return estaciones;
    }

    // genera el archivo de mediciones del día, una línea por medición
    public List<String> generarMediciones(Estacion[] estaciones, int lineas, LocalDate fecha) {
        List<String> resultado = new ArrayList<>(lineas);
        for (int i = 0; i < lineas; i++) {
            Estacion e = estaciones[random.nextInt(estaciones.length)];
            int hora = random.nextInt(24);
            int minuto = 15 * random.nextInt(4);
            double temperatura = random.nextInt(451) / 10.0 - 5.0;   // -5.0 a 40.0 °C
            int humedad = 10 + random.nextInt(91);                   // 10 a 100 %
            double precipitacion = random.nextInt(10) < 7 ? 0.0 : random.nextInt(500) / 10.0;
            resultado.add(String.format(Locale.ROOT, "%s;%s;%02d:%02d;T=%.1f;H=%d;P=%.1f;BAT=%d",
                    e.getCodigo(), fecha, hora, minuto, temperatura, humedad, precipitacion, e.getBateria()));
        }
        return resultado;
    }
}
