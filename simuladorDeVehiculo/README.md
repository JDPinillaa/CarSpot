# Simulador de Vehículos CarSpot

Aplicación de escritorio Java/Swing con dinámica longitudinal propia. El perfil
activo es el Volkswagen Golf GTI Mk8.5 europeo (265 PS, tracción delantera,
DSG7). La procedencia y el grado de confianza de cada parámetro se documentan
en [`docs/referencia-golf-gti-mk8-5.md`](docs/referencia-golf-gti-mk8-5.md).

## Requisitos y ejecución

Requiere JDK 22 o compatible. Para compilar desde esta carpeta:

```bash
mkdir -p /tmp/carspot-java-build
find src -name '*.java' -print0 | xargs -0 javac --release 22 -d /tmp/carspot-java-build
```

Desde la raíz del repositorio, inicia la aplicación con:

```bash
java -cp /tmp/carspot-java-build autonoma.main.Main
```

También puedes abrir esta carpeta como proyecto Ant en NetBeans. La aplicación
localiza `data/especificaciones.txt` desde el directorio de ejecución o desde
la ruta de las clases compiladas.

## Perfil del vehículo

Los parámetros configurables están en `data/especificaciones.txt`. El perfil
actual incluye masa, radio de rueda, curva de resistencia, adherencia, reparto
de peso, altura del centro de gravedad, batalla, freno y relaciones de caja.
Añade un bloque `perfil.<id>.<clave>=<valor>` para configurar otro vehículo y
un modelo de motor/transmisión cuando requiera una curva o lógica específica.
`perfilActivo` selecciona el vehículo inicial.

Los datos de Golf se clasifican como publicados, derivados o estimados. Las
relaciones de la DQ381 y varios parámetros de pérdidas y adherencia siguen
siendo provisionales; no deben citarse como especificaciones oficiales.

## Controles y tablero

- Usa **Encender / Apagar** para controlar el motor.
- Mantén presionado **Acelerador** o **Freno** para aplicar cada pedal.
- La caja automática cambia según las revoluciones y la demanda; la caja manual
  permite subir y bajar marcha.
- El tablero muestra velocidad en km/h, revoluciones por minuto y marcha actual.
- Los interiores clásico y deportivo cambian la presentación de los indicadores.

## Organización del código

| Paquete / clase | Responsabilidad |
| --- | --- |
| `models.Carro` | Estado e integración del vehículo, separado de Swing y audio |
| `models.PerfilVehiculo` | Ensamble del vehículo y su presentación |
| `models.motores.Motor` / `CurvaParMotor` | Régimen, configuración base y curva interpolada de par |
| `models.motores.MotorGolfGtiMk85` | Curva y parámetros del EA888 evo4 (265 PS) |
| `models.transmisiones.CajaCambios` | Contrato de transmisión y cajas concretas (`CajaDsg7GolfGti`, `CajaAutomatica`, `CajaManual`) |
| `models.fisica.ResistenciaCarretera` | Rodadura y resistencia aerodinámica |
| `models.fisica.ModeloAdherenciaLongitudinal` | Límite de tracción y fuerza de frenado |
| `persistence.PersistenciaVehiculos` | Lectura y validación de perfiles |
| `views.Cabina` | Ventana, controles y ciclo de actualización |
| `views.InteriorFactory` | Composición visual de los interiores |
| `views.IndicadorCircular` | Indicadores de velocidad y revoluciones |
| `sounds.ReproductorAudio` | Efectos de sonido opcionales |
