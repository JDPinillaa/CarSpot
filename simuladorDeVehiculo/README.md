# Simulador de Vehículos CarSpot

**Versión 1.1** · Aplicación de escritorio Java/Swing con dinámica longitudinal propia. Incluye dos
vehículos implementados como subclases de `Carro` en `autonoma.models.vehiculos`:
- **Volkswagen Golf GTI Mk8.5 europeo** (265 PS, tracción delantera, DSG7):
  [`docs/referencia-golf-gti-mk8-5.md`](docs/referencia-golf-gti-mk8-5.md)
- **Subaru BRZ (ZD8) 2.4** (234 PS, tracción trasera, manual 6 velocidades):
  [`docs/referencia-subaru-brz-zd8.md`](docs/referencia-subaru-brz-zd8.md)

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

También puedes abrir esta carpeta como proyecto Ant en NetBeans.

## Pruebas automáticas

La carpeta `test/` contiene una suite determinista sin dependencias externas.
Compila el simulador y ejecútala desde la raíz del repositorio con:

```bash
./simuladorDeVehiculo/pruebas.sh
```

O con Ant:

```bash
ant pruebas
```

Comprueba aceleraciones, velocidad máxima, frenada, protección de
sobrerrevoluciones, rango de marchas de la DSG y apagado en marcha. Devuelve un
código de salida distinto de cero si falla alguna comprobación.

## Modelo de vehículos

Cada automóvil extiende la clase abstracta `models.Carro` dentro del paquete
`models.vehiculos`, encapsulando su motor, transmisión, masa, radio de rueda,
curva de resistencia en carretera y modelo de adherencia longitudinal.
`CatalogoVehiculos` registra los vehículos disponibles en el selector inicial.

Los datos de cada vehículo se clasifican como publicados, derivados o estimados
en la carpeta `docs/`.

## Controles y tablero

- Usa **Encender / Apagar** para controlar el motor. Al apagarlo en movimiento con
  una marcha engranada, la transmisión sigue conectada y el motor frena por
  fricción y bombeo: desde 100 km/h el vehículo se detiene en unos 36–38 s.
- Mantén presionado **Acelerador** o **Freno**, con el mouse o con las teclas
  **`W`** y **`S`**. Los pedales se liberan al perder el foco de la ventana.
- La caja automática cambia según las revoluciones y la demanda; la caja manual
  permite subir y bajar marcha con los botones en pantalla o las teclas **`+`** y **`-`**.
- El tablero muestra velocidad en km/h, revoluciones por minuto y marcha actual.

## Organización del código

| Paquete / clase | Responsabilidad |
| --- | --- |
| `models.Carro` | Clase abstracta con estado e integración longitudinal del vehículo |
| `models.vehiculos.GolfGtiMk85` / `SubaruBrzZd8` | Especificaciones y ensamble concreto de cada vehículo |
| `models.vehiculos.CatalogoVehiculos` | Registro de vehículos disponibles al iniciar |
| `models.motores.Motor` / `CurvaParMotor` | Régimen, configuración base y curva interpolada de par |
| `models.motores.MotorGolfGtiMk85` / `MotorSubaruBrzZd8` | Curvas de par de los motores EA888 evo4 (265 PS) y FA24D (234 PS) |
| `models.transmisiones.CajaCambios` | Contrato de transmisión y cajas concretas (`CajaDsg7GolfGti`, `CajaAutomatica`, `CajaManual`) |
| `models.fisica.ResistenciaCarretera` | Rodadura y resistencia aerodinámica |
| `models.fisica.ModeloAdherenciaLongitudinal` / `TipoTraccion` | Límite de tracción según eje motriz (FWD/RWD/AWD) y fuerza de frenado |
| `views.Cabina` | Ventana, controles, atajos de teclado y ciclo de actualización |
| `views.InteriorFactory` | Construcción visual del tablero de instrumentos |
| `views.IndicadorCircular` | Indicadores de velocidad y revoluciones |
| `sounds.ReproductorAudio` | Efectos de sonido opcionales |
| `autonoma.tests.PruebasSimulador` | Pruebas deterministas del modelo, sin dependencias |
