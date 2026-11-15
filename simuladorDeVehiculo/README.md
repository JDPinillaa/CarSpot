# Simulador de Vehículo · versión Java

Primera versión académica de CarSpot, implementada con Java y Swing. Esta etapa
conserva el modelo original de motor, llantas, patinaje y accidentes, ahora
separado de la interfaz y preparado para configurar varios vehículos.

## Ejecutar

Requiere JDK 22 o compatible. Abre esta carpeta como proyecto Ant en NetBeans y
ejecuta `autonoma.main.Main`. El programa busca `data/especificaciones.txt`
desde el directorio de ejecución y desde la ubicación de las clases, por lo que
puedes iniciarlo desde esta carpeta o desde la raíz del repositorio. Al iniciar
permite seleccionar uno de los perfiles configurados.

También se puede compilar desde la carpeta del proyecto:

```bash
mkdir -p /tmp/carspot-java-build
find src -name '*.java' -print0 | xargs -0 javac --release 22 -d /tmp/carspot-java-build
```

## Perfiles de vehículo

`data/especificaciones.txt` define los vehículos con pares `clave=valor`:

```properties
perfilActivo=urbano

perfil.urbano.nombre=CarSpot Urbano
perfil.urbano.motor=1000
perfil.urbano.llantas=Buenas
perfil.urbano.transmision=automatica
perfil.urbano.interior=clasico

perfil.deportivo.nombre=CarSpot Deportivo
perfil.deportivo.motor=3000
perfil.deportivo.llantas=Bonitas
perfil.deportivo.transmision=manual
perfil.deportivo.interior=deportivo
```

Para agregar otro vehículo, agrega un bloque con un identificador nuevo y las
claves `nombre`, `motor`, `llantas`, `transmision` e `interior`. Los motores
disponibles son `1000`, `2000` y `3000`; los neumáticos son `Baratas`, `Buenas`
y `Bonitas`; las transmisiones son `manual` o `automatica`; y los interiores
disponibles son `clasico` y `deportivo`. `perfilActivo` marca cuál aparece
seleccionado inicialmente.

## Controles y tablero

- **Encender / Apagar** controla el estado del motor.
- **Acelerar / Frenar** solicita un cambio de velocidad en km/h.
- La **caja manual** tiene botones para subir y bajar marcha; cada marcha limita
  la velocidad alcanzable hasta que se cambie. La **automática** cambia según
  las revoluciones del motor.
- El tablero muestra un velocímetro en km/h, tacómetro en rpm y la marcha actual.
- El interior clásico y el deportivo comparten los mismos medidores y reglas;
  cambian su composición, colores y dimensiones desde `InteriorFactory`.

La simulación mantiene una escala académica por acciones discretas del usuario;
todavía no modela aceleración física continua. La arquitectura separa el
vehículo, su transmisión y su presentación para que el modelo de conducción se
pueda ampliar después sin rehacer los interiores ni la ventana principal.

## Organización del código

| Paquete / clase | Responsabilidad |
| --- | --- |
| `models.Carro` | Estado y reglas del vehículo, sin Swing ni reproducción de audio |
| `models.Motor`, `models.Llanta` | Capacidades del motor y límite de patinaje |
| `models.CajaCambios` | Contrato de transmisión; implementaciones manual y automática |
| `models.PerfilVehiculo` | Ensamble de motor, llantas, caja e interior |
| `persistence.PersistenciaVehiculos` | Lectura y validación del catálogo configurable |
| `views.Cabina` | Ventana y controles que invocan el modelo |
| `views.InteriorFactory` | Composición de cada variante de cabina |
| `views.IndicadorCircular` | Medidor reutilizable para velocidad y revoluciones |
| `sounds.ReproductorAudio` | Efectos de sonido opcionales, fuera del modelo |

Para crear una nueva transmisión, implementa `CajaCambios`; para añadir un
interior, agrega una variante a `InteriorFactory` y valida su identificador en
`PerfilVehiculo`. La clase `Cabina` dejó de usar el formulario generado de
NetBeans; `Cabina.form` se conserva por ahora como archivo histórico y no debe
usarse para regenerar la ventana.
