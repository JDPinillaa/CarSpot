package autonoma.persistence;

import autonoma.models.Carro;
import autonoma.models.PerfilVehiculo;
import autonoma.models.fisica.ModeloAdherenciaLongitudinal;
import autonoma.models.fisica.ResistenciaCarretera;
import autonoma.models.fisica.TipoTraccion;
import autonoma.models.motores.Motor;
import autonoma.models.motores.MotorGolfGtiMk85;
import autonoma.models.motores.MotorSubaruBrzZd8;
import autonoma.models.transmisiones.CajaAutomatica;
import autonoma.models.transmisiones.CajaCambios;
import autonoma.models.transmisiones.CajaDsg7GolfGti;
import autonoma.models.transmisiones.CajaManual;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Lee perfiles de vehículos desde un archivo clave/valor extensible. */
public final class PersistenciaVehiculos {
    private static final Path RUTA_CONFIGURACION = Path.of("data", "especificaciones.txt");

    private PersistenciaVehiculos() { }

    public static CatalogoVehiculos cargarCatalogo() throws IOException {
        Path configuracion = localizarConfiguracion();
        Map<String, String> valores = leerValores(configuracion);
        Map<String, Map<String, String>> perfiles = new LinkedHashMap<>();
        for (Map.Entry<String, String> valor : valores.entrySet()) {
            String[] partes = valor.getKey().split("\\.", 3);
            if (partes.length == 3 && partes[0].equals("perfil")) {
                perfiles.computeIfAbsent(partes[1], ignorado -> new LinkedHashMap<>())
                        .put(partes[2], valor.getValue());
            }
        }

        List<PerfilVehiculo> lista = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> perfil : perfiles.entrySet()) {
            String id = perfil.getKey();
            Map<String, String> datos = perfil.getValue();
            Motor motor = crearMotor(requerido(datos, "motor", id));
            CajaCambios transmision = crearTransmision(datos);
            String nombre = datos.getOrDefault("nombre", id);
            String interior = datos.getOrDefault("interior", "clasico");
            double masaKg = decimal(datos, "masa", 1300);
            double radioRuedaM = decimal(datos, "radiorueda", 0.315);
            TipoTraccion traccion = crearTraccion(datos.getOrDefault("traccion", "delantera"));

            ResistenciaCarretera resistencia = crearResistencia(datos, id, masaKg);
            ModeloAdherenciaLongitudinal adherencia = new ModeloAdherenciaLongitudinal(
                    masaKg,
                    decimal(datos, "adherencialongitudinal", 0.95),
                    decimal(datos, "repartopesodelantero", 0.60),
                    decimal(datos, "alturacentrogravedad", 0.55),
                    decimal(datos, "batalla", 2.50),
                    decimal(datos, "aceleracionmaximafrenadog", 0.92),
                    traccion);

            Carro carro = new Carro(motor, transmision, masaKg, radioRuedaM, resistencia, adherencia);
            lista.add(new PerfilVehiculo(id, nombre, interior, carro));
        }
        return new CatalogoVehiculos(lista, valores.getOrDefault("perfilactivo", lista.get(0).getId()));
    }

    private static TipoTraccion crearTraccion(String valor) throws IOException {
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "delantera", "fwd" -> TipoTraccion.DELANTERA;
            case "trasera", "rwd" -> TipoTraccion.TRASERA;
            case "integral", "awd" -> TipoTraccion.INTEGRAL;
            default -> throw new IOException("Tipo de tracción no reconocido: " + valor);
        };
    }

    private static ResistenciaCarretera crearResistencia(Map<String, String> datos, String id, double masaKg)
            throws IOException {
        boolean usaCurvaResistencia = datos.containsKey("resistenciaconstanten")
                || datos.containsKey("resistencialinealnporkmh")
                || datos.containsKey("resistenciacuadraticanporkmh2");
        if (usaCurvaResistencia) {
            if (!datos.containsKey("resistenciaconstanten")
                    || !datos.containsKey("resistencialinealnporkmh")
                    || !datos.containsKey("resistenciacuadraticanporkmh2")) {
                throw new IOException("El perfil " + id
                        + " requiere los tres coeficientes de resistencia a la carretera.");
            }
            return ResistenciaCarretera.desdeCoeficientes(
                    decimal(datos, "resistenciaconstanten", 0),
                    decimal(datos, "resistencialinealnporkmh", 0),
                    decimal(datos, "resistenciacuadraticanporkmh2", 0));
        }
        return ResistenciaCarretera.desdeCda(
                masaKg,
                decimal(datos, "cda", 0.70),
                decimal(datos, "rodadura", 0.015));
    }

    private static Map<String, String> leerValores(Path configuracion) throws IOException {
        Map<String, String> valores = new LinkedHashMap<>();
        for (String linea : Files.readAllLines(configuracion)) {
            String limpia = linea.strip();
            if (limpia.isEmpty() || limpia.startsWith("#")) continue;
            int separador = limpia.indexOf('=');
            String clave;
            String valor;
            if (separador >= 0) {
                clave = limpia.substring(0, separador).trim();
                valor = limpia.substring(separador + 1).trim();
            } else {
                String[] partes = limpia.split("\\s+", 2);
                if (partes.length != 2) continue;
                clave = partes[0];
                valor = partes[1].trim();
            }
            valores.put(clave.toLowerCase(Locale.ROOT), valor);
        }
        return valores;
    }

    /**
     * Busca el archivo tanto desde el directorio de ejecución como desde la
     * ubicación de las clases. Así funciona al ejecutar desde el repo, el
     * proyecto Ant o su carpeta build/classes.
     */
    private static Path localizarConfiguracion() throws IOException {
        Set<Path> bases = new LinkedHashSet<>();
        agregarAncestros(Path.of(System.getProperty("user.dir", ".")), bases);
        try {
            Path clases = Path.of(PersistenciaVehiculos.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            agregarAncestros(clases, bases);
        } catch (URISyntaxException | SecurityException | NullPointerException ignorada) {
            // Si el runtime no expone la ubicación de las clases, queda la búsqueda desde user.dir.
        }

        for (Path base : bases) {
            Path directa = base.resolve(RUTA_CONFIGURACION);
            if (Files.isRegularFile(directa)) return directa;

            Path desdeRaiz = base.resolve("simuladorDeVehiculo").resolve(RUTA_CONFIGURACION);
            if (Files.isRegularFile(desdeRaiz)) return desdeRaiz;
        }

        throw new IOException("No se encontró " + RUTA_CONFIGURACION
                + ". Ejecuta desde la carpeta del proyecto o la raíz del repositorio.");
    }

    private static void agregarAncestros(Path ruta, Set<Path> destinos) {
        Path actual = ruta.toAbsolutePath().normalize();
        if (!Files.isDirectory(actual)) actual = actual.getParent();
        while (actual != null) {
            destinos.add(actual);
            actual = actual.getParent();
        }
    }

    private static String requerido(Map<String, String> datos, String clave, String perfil) throws IOException {
        String valor = datos.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new IOException("Falta '" + clave + "' en el perfil '" + perfil + "' de la configuración.");
        }
        return valor;
    }

    private static Motor crearMotor(String tipo) throws IOException {
        return switch (tipo.trim()) {
            case "golf-gti-mk8-5" -> new MotorGolfGtiMk85();
            case "subaru-brz-zd8" -> new MotorSubaruBrzZd8();
            default -> throw new IOException("Motor no reconocido: " + tipo);
        };
    }

    private static CajaCambios crearTransmision(Map<String, String> datos) throws IOException {
        String tipo = datos.getOrDefault("transmision", "automatica");
        boolean tieneRelaciones = datos.containsKey("relacionesmarcha");
        double factorEntrega = decimal(datos, "factorentregaparefectivo", 0.90);
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "manual" -> {
                if (!tieneRelaciones) yield new CajaManual();
                double[] relaciones = listaDecimales(datos, "relacionesmarcha");
                double[] finales = resolverFinales(datos, relaciones.length);
                yield new CajaManual(relaciones, finales, factorEntrega);
            }
            case "automatica", "automática" -> {
                if (!tieneRelaciones) yield new CajaAutomatica();
                double[] relaciones = listaDecimales(datos, "relacionesmarcha");
                double[] finales = resolverFinales(datos, relaciones.length);
                yield new CajaAutomatica(relaciones, finales, factorEntrega);
            }
            case "dsg7" -> {
                if (!tieneRelaciones && !datos.containsKey("relacionesfinales")) {
                    yield new CajaDsg7GolfGti();
                }
                double[] relaciones = listaDecimales(datos, "relacionesmarcha");
                double[] finales = resolverFinales(datos, relaciones.length);
                if (relaciones.length != 7) {
                    throw new IOException("La DSG7 requiere siete relaciones de marcha.");
                }
                yield new CajaDsg7GolfGti(relaciones, finales, factorEntrega);
            }
            default -> throw new IOException("Transmisión no reconocida: " + tipo);
        };
    }

    private static double[] resolverFinales(Map<String, String> datos, int numeroMarchas) throws IOException {
        if (datos.containsKey("relacionesfinales")) {
            double[] finales = listaDecimales(datos, "relacionesfinales");
            if (finales.length != numeroMarchas) {
                throw new IOException("La cantidad de relaciones finales debe coincidir con las marchas.");
            }
            return finales;
        }
        if (datos.containsKey("relacionfinal")) {
            double relacionFinal = decimal(datos, "relacionfinal", 3.90);
            double[] finales = new double[numeroMarchas];
            Arrays.fill(finales, relacionFinal);
            return finales;
        }
        throw new IOException("Falta 'relacionFinal' o 'relacionesFinales' para la transmisión configurada.");
    }

    private static double[] listaDecimales(Map<String, String> datos, String clave) throws IOException {
        String valor = datos.get(clave);
        if (valor == null || valor.isBlank()) throw new IOException("Falta la lista '" + clave + "'.");
        String[] elementos = valor.split(",");
        double[] resultado = new double[elementos.length];
        for (int i = 0; i < elementos.length; i++) {
            try {
                resultado[i] = Double.parseDouble(elementos[i].trim());
            } catch (NumberFormatException error) {
                throw new IOException("Valor numérico inválido en '" + clave + "': " + elementos[i], error);
            }
        }
        return resultado;
    }

    private static double decimal(Map<String, String> datos, String clave, double valorPredeterminado)
            throws IOException {
        String valor = datos.get(clave);
        if (valor == null) return valorPredeterminado;
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException error) {
            throw new IOException("Valor numérico inválido para '" + clave + "': " + valor, error);
        }
    }
}
