package autonoma.tests;

import autonoma.models.Carro;
import autonoma.models.fisica.ResistenciaCarretera;
import autonoma.models.vehiculos.CatalogoVehiculos;
import java.util.List;

/**
 * Pruebas deterministas del modelo longitudinal, sin dependencias externas.
 *
 * <p>Se ejecutan con un paso fijo de 10 ms y comprueban que las prestaciones
 * documentadas sigan dentro de rangos razonables. No prueban la interfaz Swing;
 * su objetivo es detectar regresiones en la física, las transmisiones y el
 * apagado del motor.</p>
 */
public final class PruebasSimulador {
    private static final double DT = 0.01;
    private static final double MARGEN_RPM_CAMBIO = 150;

    private static int pruebas;
    private static int fallos;

    private PruebasSimulador() { }

    public static void main(String[] args) {
        System.out.println("CarSpot · pruebas del simulador (paso " + DT + " s)");
        System.out.println();

        pruebaPrestacionesGolf();
        pruebaPrestacionesBrz();
        pruebaFrenada();
        pruebaApagadoEnMarcha();
        pruebaProteccionManual();
        pruebaCajaDsg();
        pruebaResistenciaCarretera();

        System.out.println();
        System.out.printf("%d pruebas, %d fallos%n", pruebas, fallos);
        if (fallos > 0) {
            System.exit(1);
        }
    }

    private static void pruebaPrestacionesGolf() {
        Carro golf = buscar("Golf");

        double ceroCien = acelerarHasta(golf, 100, 30);
        comprobar("Golf 0–100 km/h", enRango(ceroCien, 5.4, 6.1),
                String.format("%.2f s (esperado 5,4–6,1 s)", ceroCien));

        Carro golfDoscientos = buscar("Golf");
        double ceroDoscientos = acelerarHasta(golfDoscientos, 200, 40);
        comprobar("Golf 0–200 km/h", enRango(ceroDoscientos, 20.0, 22.0),
                String.format("%.2f s (esperado 20,0–22,0 s)", ceroDoscientos));

        Carro golfMaxima = buscar("Golf");
        double maxima = velocidadTrasAcelerar(golfMaxima, 140);
        comprobar("Golf velocidad máxima", enRango(maxima, 249.5, 250.5),
                String.format("%.1f km/h (limitador 250 km/h)", maxima));
    }

    private static void pruebaPrestacionesBrz() {
        Carro brz = buscar("BRZ");

        double ceroCien = acelerarHasta(brz, 100, 30);
        comprobar("BRZ 0–100 km/h", enRango(ceroCien, 6.0, 6.6),
                String.format("%.2f s (esperado 6,0–6,6 s)", ceroCien));

        Carro brzDoscientos = buscar("BRZ");
        double ceroDoscientos = acelerarHasta(brzDoscientos, 200, 40);
        comprobar("BRZ 0–200 km/h", enRango(ceroDoscientos, 24.0, 26.0),
                String.format("%.2f s (esperado 24,0–26,0 s)", ceroDoscientos));

        Carro brzMaxima = buscar("BRZ");
        double maxima = velocidadTrasAcelerar(brzMaxima, 140);
        comprobar("BRZ velocidad máxima", enRango(maxima, 225.5, 226.5),
                String.format("%.1f km/h (máxima publicada 226 km/h)", maxima));
    }

    private static void pruebaFrenada() {
        Carro golf = buscar("Golf");
        double[] frenadaGolf = frenarDesde100(golf);
        comprobar("Golf frenada 100–0", enRango(frenadaGolf[1], 35.5, 37.0)
                        && enRango(frenadaGolf[0], 2.2, 3.2),
                String.format("%.2f s, %.2f m", frenadaGolf[0], frenadaGolf[1]));

        Carro brz = buscar("BRZ");
        double[] frenadaBrz = frenarDesde100(brz);
        comprobar("BRZ frenada 100–0", enRango(frenadaBrz[1], 32.5, 34.5)
                        && enRango(frenadaBrz[0], 2.1, 3.0),
                String.format("%.2f s, %.2f m", frenadaBrz[0], frenadaBrz[1]));
    }

    private static void pruebaApagadoEnMarcha() {
        Carro detenido = buscar("Golf");
        detenido.encender();
        detenido.apagar();
        detenido.setAcelerador(1);
        avanzar(detenido, 3);
        comprobar("Motor apagado no entrega tracción", detenido.getVelocidadMps() == 0,
                String.format("%.2f m/s tras 3 s", detenido.getVelocidadMps()));

        Carro golf = buscar("Golf");
        acelerarHasta(golf, 100, 30);
        golf.setAcelerador(0);
        golf.apagar();
        double distancia = 0;
        double tiempo = 0;
        double velocidadAnterior = golf.getVelocidadMps();
        while (golf.getVelocidadMps() > 0 && tiempo < 60) {
            distancia += velocidadAnterior * DT;
            avanzarUnPaso(golf);
            tiempo += DT;
            velocidadAnterior = golf.getVelocidadMps();
        }
        comprobar("Apagado en marcha se detiene", enRango(tiempo, 25.0, 50.0),
                String.format("%.1f s y %.0f m desde 100 km/h (sin tracción, con freno motor)",
                        tiempo, distancia));

        Carro brz = buscar("BRZ");
        acelerarHasta(brz, 100, 30);
        brz.setAcelerador(0);
        double antes = brz.getVelocidadMps();
        brz.apagar();
        brz.setAcelerador(1);
        avanzar(brz, 2);
        comprobar("Acelerador ignorado con motor apagado", brz.getVelocidadMps() < antes,
                String.format("%.1f → %.1f km/h", antes * 3.6, brz.getVelocidadMps() * 3.6));
    }

    private static void pruebaProteccionManual() {
        Carro brz = buscar("BRZ");
        acelerarHasta(brz, 160, 40);
        int marchaAntes = brz.getMarchaActual();
        for (int i = 0; i < 10; i++) {
            brz.cambiarMarcha(-1);
        }
        comprobar("Manual protege sobrerrevoluciones al reducir",
                brz.getMarchaActual() == marchaAntes && brz.getRevoluciones() <= brz.getRevolucionesMaximas(),
                String.format("marcha %d, %d rpm", brz.getMarchaActual(), brz.getRevoluciones()));
    }

    private static void pruebaCajaDsg() {
        Carro golf = buscar("Golf");
        golf.encender();
        golf.setAcelerador(1);
        boolean rangoValido = true;
        for (int i = 0; i < 6000; i++) {
            avanzarUnPaso(golf);
            if (golf.getMarchaActual() < 1 || golf.getMarchaActual() > golf.getNumeroMarchas()) {
                rangoValido = false;
                break;
            }
        }
        comprobar("DSG mantiene marchas válidas", rangoValido,
                "marchas 1–" + golf.getNumeroMarchas() + " durante 60 s a fondo");

        Carro frenada = buscar("Golf");
        acelerarHasta(frenada, 100, 30);
        frenada.setAcelerador(0);
        frenada.setFreno(1);
        boolean noNegativa = true;
        for (int i = 0; i < 1000; i++) {
            avanzarUnPaso(frenada);
            if (frenada.getVelocidadMps() < 0) {
                noNegativa = false;
                break;
            }
        }
        comprobar("La velocidad nunca es negativa", noNegativa,
                "frenada completa desde 100 km/h");
    }

    private static void pruebaResistenciaCarretera() {
        ResistenciaCarretera resistencia = ResistenciaCarretera.desdeCoeficientes(150, 2.5, 0.4);
        double aCincuenta = resistencia.calcularFuerzaN(50 / 3.6);
        double aCientoVeinte = resistencia.calcularFuerzaN(120 / 3.6);
        boolean valida = resistencia.calcularFuerzaN(0) == 0
                && resistencia.calcularFuerzaN(-5) == 0
                && aCincuenta > 0
                && aCientoVeinte > aCincuenta;
        comprobar("Resistencia crece con la velocidad y se anula al reposo", valida,
                String.format("%.0f N a 50 km/h, %.0f N a 120 km/h", aCincuenta, aCientoVeinte));
    }

    private static Carro buscar(String fragmento) {
        List<Carro> vehiculos = CatalogoVehiculos.obtenerVehiculos();
        for (Carro vehiculo : vehiculos) {
            if (vehiculo.getNombre().contains(fragmento)) {
                return vehiculo;
            }
        }
        throw new IllegalStateException("No hay un vehículo que contenga «" + fragmento + "».");
    }

    /** Acelera a fondo y devuelve el tiempo hasta la velocidad indicada, o -1. */
    private static double acelerarHasta(Carro carro, double kmh, double limiteS) {
        carro.encender();
        carro.setFreno(0);
        carro.setAcelerador(1);
        double tiempo = 0;
        while (tiempo < limiteS) {
            avanzarUnPaso(carro);
            tiempo += DT;
            if (carro.getVelocidadMps() * 3.6 >= kmh) {
                return tiempo;
            }
        }
        return -1;
    }

    private static double velocidadTrasAcelerar(Carro carro, double segundos) {
        carro.encender();
        carro.setFreno(0);
        carro.setAcelerador(1);
        avanzar(carro, segundos);
        return carro.getVelocidadMps() * 3.6;
    }

    /** Devuelve {tiempo, distancia} de una frenada a fondo desde 100 km/h. */
    private static double[] frenarDesde100(Carro carro) {
        acelerarHasta(carro, 100, 30);
        carro.setAcelerador(0);
        carro.setFreno(1);
        double tiempo = 0;
        double distancia = 0;
        while (carro.getVelocidadMps() > 0 && tiempo < 20) {
            double velocidadAntes = carro.getVelocidadMps();
            avanzarUnPaso(carro);
            double velocidadDespues = carro.getVelocidadMps();
            distancia += (velocidadAntes + velocidadDespues) * 0.5 * DT;
            tiempo += DT;
        }
        return new double[]{tiempo, distancia};
    }

    /** Avanza un paso; en cajas manuales sube de marcha cerca del corte. */
    private static void avanzarUnPaso(Carro carro) {
        carro.actualizar(DT);
        if (carro.isManual() && carro.getRevoluciones() >= carro.getRevolucionesMaximas() - MARGEN_RPM_CAMBIO
                && carro.getMarchaActual() < carro.getNumeroMarchas()) {
            carro.cambiarMarcha(1);
        }
    }

    private static void avanzar(Carro carro, double segundos) {
        int pasos = Math.max(1, (int) Math.round(segundos / DT));
        for (int i = 0; i < pasos; i++) {
            avanzarUnPaso(carro);
        }
    }

    private static boolean enRango(double valor, double minimo, double maximo) {
        return Double.isFinite(valor) && valor >= minimo && valor <= maximo;
    }

    private static void comprobar(String nombre, boolean condicion, String detalle) {
        pruebas++;
        if (condicion) {
            System.out.printf("  OK    %-44s %s%n", nombre, detalle);
        } else {
            fallos++;
            System.out.printf("  FALLO %-44s %s%n", nombre, detalle);
        }
    }
}
