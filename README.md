# CarSpot · Simulador Longitudinal de Vehículos

**CarSpot** es un simulador de escritorio desarrollado en **Java 22 y Swing** que modela la dinámica longitudinal de vehículos reales en tiempo real. A diferencia de un ejercicio discreto por pasos fijos, el proyecto integra las ecuaciones físicas de movimiento en función de curvas de par motor, relaciones de transmisión, transferencia de carga entre ejes y resistencias de rodadura y aerodinámica obtenidas de fichas técnicas oficiales y ensayos de laboratorio (*coastdown* de la EPA).

---

## ¿Qué hace el simulador?

- **Selector de garaje multi-vehículo**: Al iniciar, permite elegir entre distintos vehículos configurados de forma declarativa en [`simuladorDeVehiculo/data/especificaciones.txt`](simuladorDeVehiculo/data/especificaciones.txt).
- **Tablero de instrumentos en tiempo real**: Visualiza la velocidad ($\text{km/h}$), el régimen del motor ($\text{rpm}$), la marcha engranada y el estado del motor a $50\text{ Hz}$ ($\Delta t \approx 20\text{ ms}$).
- **Transmisiones automáticas (DSG) y manuales**:
  - En cajas **automáticas / DSG**, la transmisión gestiona las subidas de marcha según la carga del acelerador, ejecuta reducciones rápidas (*kickdown*) buscando la zona de potencia máxima y modela la caída parcial de par durante el cambio.
  - En cajas **manuales**, el conductor controla los cambios mediante los botones en pantalla o con las teclas **`+`** y **`-`** del teclado, contando con **corte de inyección por marcha** al llegar al límite de giro y **protección contra sobrerrevoluciones** al reducir.
- **Controles continuos y audio opcional**: Acelerador y freno responden mientras se mantienen presionados, con freno motor al soltar el acelerador y efectos de sonido independientes del modelo físico.

---

## ¿Cómo lo hace? (Modelo físico y arquitectura)

### 1. Dinámica longitudinal en unidades SI

En cada paso de tiempo $\Delta t$, el vehículo ([`Carro.java`](simuladorDeVehiculo/src/autonoma/models/Carro.java)) calcula las fuerzas longitudinales que actúan sobre su masa $m$:

$$a = \frac{F_{\text{motriz}} - F_{\text{freno}} - F_{\text{resistencia}}}{m}$$

1. **Par motor e interpolación ([`Motor`](simuladorDeVehiculo/src/autonoma/models/motores/Motor.java) y [`CurvaParMotor`](simuladorDeVehiculo/src/autonoma/models/motores/CurvaParMotor.java))**:
   El par $T(\text{rpm})$ se obtiene por interpolación lineal por tramos sobre puntos publicados y derivados ($T = P / \omega$), respetando las mesetas de par y potencia y cortando la entrega al alcanzar el régimen máximo.
2. **Multiplicación del tren motriz ([`CajaCambios`](simuladorDeVehiculo/src/autonoma/models/transmisiones/CajaCambios.java))**:
   La fuerza disponible en rueda se calcula a partir de la relación de la marcha actual $i_g$, la relación del grupo final $i_f$ (única o por árbol según la arquitectura de la caja), el factor efectivo de entrega $\eta$ y el radio de rueda $r$:
   $$F_{\text{rueda}} = \frac{T(\text{rpm}) \cdot i_g \cdot i_f \cdot \eta}{r}$$
3. **Adherencia y transferencia dinámica de carga ([`ModeloAdherenciaLongitudinal`](simuladorDeVehiculo/src/autonoma/models/fisica/ModeloAdherenciaLongitudinal.java) y [`TipoTraccion`](simuladorDeVehiculo/src/autonoma/models/fisica/TipoTraccion.java))**:
   La tracción máxima depende de qué eje impulsa al vehículo, de la fracción de peso estático delantero $f_{\text{del}}$, de la altura del centro de gravedad $h_{\text{CG}}$ y de la batalla $L$:
   - **Tracción delantera (`DELANTERA`)**: Al acelerar se descarga el eje delantero:
     $$F_{\text{tracción, máx}} = m g \frac{\mu \cdot f_{\text{del}}}{1 + \mu \cdot h_{\text{CG}} / L}$$
   - **Tracción trasera (`TRASERA`)**: Al acelerar se transfiere carga hacia el eje trasero motriz:
     $$F_{\text{tracción, máx}} = m g \frac{\mu \cdot (1 - f_{\text{del}})}{1 - \mu \cdot h_{\text{CG}} / L}$$
   - **Tracción integral (`INTEGRAL`)**: Aprovecha la carga normal total ($F_{\text{tracción, máx}} = \mu m g$).
4. **Resistencia en carretera ([`ResistenciaCarretera`](simuladorDeVehiculo/src/autonoma/models/fisica/ResistenciaCarretera.java))**:
   Admite tanto el modelo aerodinámico/rodadura clásico ($C_d A$ y $C_{rr}$) como polinomios de desaceleración en carretera (*coastdown* de la EPA) en función de la velocidad $v$ en $\text{km/h}$:
   $$F_{\text{resistencia}}(v) = F_0 + F_1 v + F_2 v^2$$

### 2. Arquitectura desacoplada

El código separa estrictamente la simulación física de la interfaz gráfica y la configuración:

| Paquete | Responsabilidad |
| --- | --- |
| `autonoma.models` | Estado del vehículo (`Carro`), integración temporal y perfil ensamblado (`PerfilVehiculo`), sin dependencias de Swing ni de audio |
| `autonoma.models.fisica` | Modelos físicos reutilizables: `ResistenciaCarretera`, `ModeloAdherenciaLongitudinal` y `TipoTraccion` |
| `autonoma.models.motores` | Clase base `Motor`, interpolador `CurvaParMotor` y curvas específicas (`MotorGolfGtiMk85`, `MotorSubaruBrzZd8`) |
| `autonoma.models.transmisiones` | Contrato `CajaCambios`, base `AbstractCajaCambios` y transmisiones (`CajaDsg7GolfGti`, `CajaManual`, `CajaAutomatica`) |
| `autonoma.persistence` | Carga, validación y ensamble declarativo de perfiles desde `data/especificaciones.txt` (`PersistenciaVehiculos`, `CatalogoVehiculos`) |
| `autonoma.views` | Interfaz gráfica Swing (`Cabina`, `InteriorFactory`, `IndicadorCircular`) y atajos de teclado |
| `autonoma.sounds` | Reproducción opcional de efectos de audio (`ReproductorAudio`) |

---

## Vehículos incluidos

Cada perfil separa explícitamente en su documentación qué datos son **oficiales de fábrica**, cuáles son **derivados** y cuáles son **estimados o calibrados**:

| Vehículo | Motor | Potencia / Par | Transmisión y Tracción | Masa | 0–100 km/h (Sim. / Oficial) | Documentación técnica |
| --- | --- | --- | --- | ---: | :---: | --- |
| **Volkswagen Golf GTI Mk8.5** | 2.0 TSI (EA888 evo4) | 195 kW (265 PS) / 370 Nm | DSG 7 vel. (`DQ381-7F`) · Delantera (FWD) | 1.457 kg | 5,75 s / 5,9 s | [Referencia Golf GTI Mk8.5](simuladorDeVehiculo/docs/referencia-golf-gti-mk8-5.md) |
| **Subaru BRZ (ZD8)** | 2.4 Boxer (FA24D) | 172 kW (234 PS) / 250 Nm | Manual 6 vel. (`TL70`) · Trasera (RWD) | 1.275 kg | 6,29 s / 6,3 s | [Referencia Subaru BRZ ZD8](simuladorDeVehiculo/docs/referencia-subaru-brz-zd8.md) |

---

## Requisitos y ejecución

- **JDK 22** o compatible.
- Sin dependencias externas.

### Compilar y ejecutar desde la terminal

Desde la raíz del repositorio:

```bash
mkdir -p /tmp/carspot-java-build
find simuladorDeVehiculo/src -name '*.java' -print0 | xargs -0 javac --release 22 -d /tmp/carspot-java-build
java -cp /tmp/carspot-java-build autonoma.main.Main
```

También se puede abrir [`simuladorDeVehiculo/`](simuladorDeVehiculo/) directamente como proyecto **Ant** en NetBeans o IntelliJ IDEA. El cargador localiza `data/especificaciones.txt` tanto desde la raíz del repositorio como desde la carpeta del proyecto o de las clases compiladas.

### Controles en la cabina

- **Encender / Apagar**: Arranca o detiene el motor. Si se apaga en movimiento, el vehículo pierde tracción y desacelera por las resistencias físicas.
- **Acelerador · mantener / Freno · mantener**: Mantén presionado con el mouse para aplicar demanda continua.
- **Cambio de marcha (vehículos manuales)**: Botones **`－ Marcha`** y **`＋ Marcha`** en pantalla, o teclas **`-`** y **`+`** del teclado.
- **Estado**: Muestra un resumen instantáneo de velocidad, régimen, transmisión y marcha actual.

---

## Cómo agregar un nuevo vehículo

1. **Curva de par motor**: Crea una subclase de [`Motor`](simuladorDeVehiculo/src/autonoma/models/motores/Motor.java) en `autonoma.models.motores` definiendo velocidad máxima, potencia, ralentí, corte y los arreglos `rpm`/`Nm`, y regístrala en [`PersistenciaVehiculos.crearMotor`](simuladorDeVehiculo/src/autonoma/persistence/PersistenciaVehiculos.java).
2. **Perfil en configuración**: Añade un bloque `perfil.<id>.*` en [`simuladorDeVehiculo/data/especificaciones.txt`](simuladorDeVehiculo/data/especificaciones.txt) indicando `motor`, `transmision` (`manual`, `automatica` o `dsg7`), `traccion` (`delantera`, `trasera` o `integral`), `relacionesMarcha`, `relacionFinal` (o `relacionesFinales`), `masa`, `radioRueda`, coeficientes de resistencia y parámetros de adherencia.
3. **Documentación**: Registra las fuentes oficiales, derivaciones y calibraciones del nuevo modelo en `simuladorDeVehiculo/docs/`.

---

## Estructura del proyecto

```text
simuladorDeVehiculo/
├── data/
│   └── especificaciones.txt          # Catálogo declarativo de perfiles y parámetros físicos
├── docs/
│   ├── referencia-golf-gti-mk8-5.md  # Fuentes, supuestos y validación del Golf GTI Mk8.5
│   └── referencia-subaru-brz-zd8.md  # Fuentes, supuestos y validación del Subaru BRZ 2.4
├── src/autonoma/
│   ├── main/                         # Punto de entrada y selector de garaje (Main)
│   ├── models/                       # Vehículo (Carro, PerfilVehiculo)
│   │   ├── fisica/                   # Resistencia en carretera, adherencia y tipo de tracción
│   │   ├── motores/                  # Motor base, interpolador de par y motores por vehículo
│   │   └── transmisiones/            # Contrato de caja y transmisiones manuales/automáticas/DSG
│   ├── persistence/                  # Lectura y validación del catálogo de vehículos
│   ├── sounds/                       # Reproductor de efectos de sonido opcionales
│   └── views/                        # Cabina Swing, tablero e indicadores circulares
├── nbproject/                        # Configuración del proyecto Ant/NetBeans
└── README.md                         # Guía rápida del módulo Java
```
