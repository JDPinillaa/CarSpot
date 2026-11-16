# Vehículo de referencia: Subaru BRZ (ZD8) 2.4 · 234 PS (6MT)

Segundo vehículo del simulador, seleccionado por contar con relaciones de caja
manual y grupo final único publicados oficialmente por el fabricante, además de
coeficientes de resistencia a la carretera (*coastdown*) en la base de datos de
la EPA para la misma motorización de 2,4 litros y 6 velocidades.

## Datos publicados por Subaru y la EPA

| Dato | Valor de referencia | Clasificación / Fuente |
| --- | ---: | --- |
| Motor y cilindrada | Bóxer 4 cilindros atmosférico FA24D, 2.387 cm³ | Oficial — [Ficha técnica Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Potencia máxima (Europa) | 172 kW / 234 PS a 7.000 rpm | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) (en EE. UU. homologa 228 hp / 170 kW a 7.000 rpm) |
| Par máximo | 250 Nm a 3.700 rpm | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Régimen de corte | 7.400 rpm | Oficial — [Subaru Media Press Kit](https://media.subaru.com/pressrelease/1801/1/all-new-2022-subaru-brz-debuts) |
| Transmisión | Manual de 6 velocidades (TL70), tracción trasera (RWD) | Oficial — [Subaru Media Press Kit](https://media.subaru.com/pressrelease/1801/1/all-new-2022-subaru-brz-debuts) |
| Relaciones de marcha (1.ª–6.ª) | 3,626 / 2,188 / 1,541 / 1,213 / 1,000 / 0,767 | Oficial — [Subaru Media / Ficha técnica](https://media.subaru.com/pressrelease/1801/1/all-new-2022-subaru-brz-debuts) |
| Relación final | 4,100 (única para las 6 marchas, diferencial Torsen) | Oficial — [Subaru Media / EPA Test Car List 2025](https://www.epa.gov/system/files/documents/2026-01/25-testcar-2026-01-21.xlsx) |
| Aceleración publicada | 0–100 km/h en 6,3 s | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Velocidad máxima | 226 km/h | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Peso en vacío (DIN, 6MT) | 1.275 kg | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Reparto de peso estático | 53 % delante / 47 % detrás | Oficial — [Subaru Media](https://media.subaru.com/pressrelease/1801/1/all-new-2022-subaru-brz-debuts) |
| Altura del centro de gravedad | 0,456 m (456 mm) | Oficial — Especificación de plataforma ZD8/ZN8 |
| Batalla | 2.575 mm (2,575 m) | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Neumático de serie (18 pulgadas) | 215/40 R18 (Michelin Pilot Sport 4) | Oficial — [Subaru Alemania](https://www.subaru.de/modelle/brz) |
| Coeficientes de resistencia EPA (`Target Coef A/B/C`) | 31,29 lbf · 0,4108 lbf/mph · 0,01631 lbf/mph² | Oficial (ensayo EPA) — [EPA 2025 Test Car List](https://www.epa.gov/system/files/documents/2026-01/25-testcar-2026-01-21.xlsx) (BRZ 2.4L 6MT, RWD, ETW 3.125 lb, N/V 43,9) |

## Parámetros derivados, estimados y calibrados

1. **Radio geométrico de rueda (Derivado: `0,3146 m`)**:
   Calculado directamente de la medida publicada `215/40 R18`:
   ```text
   r = (18 × 25,4 / 2 + 215 × 0,40) / 1000 = 0,3146 m
   ```
   No representa el radio dinámico bajo carga. Con el N/V de `43,9 rpm/mph`
   declarado ante la EPA en 6.ª (`0,767 × 4,100 = 3,1447`), el radio efectivo de
   aquel ensayo equivaldría a `0,3056 m`; se mantiene el radio geométrico
   explícito por consistencia con el resto del simulador.

2. **Curva de par motor (Puntos oficiales + interpolación estimada)**:
   Subaru publica el par máximo (`250 Nm a 3.700 rpm`), la potencia máxima en
   especificación europea (`172 kW / 234 PS a 7.000 rpm`, de donde se deriva
   exactamente `234,640 Nm` mediante `T = P / ω`) y el corte a `7.400 rpm`.
   Los puntos intermedios (`750, 1.500, 2.500, 4.500, 5.200, 6.000, 6.500 y
   7.400 rpm`) son **estimaciones** basadas en la curva característica del
   FA24D (que corrige el bache de medio régimen del antiguo FA20 de 2,0 L).

3. **Curva de resistencia en carretera (Derivada de ensayo EPA)**:
   La conversión a unidades SI/km/h de los coeficientes objetivo de carretera
   (`Target Coef A/B/C`) de la EPA para el Subaru BRZ 2.4L 6MT R18 produce:
   - `F0 = 139,1848 N` (`31,29 lbf`)
   - `F1 = 1,135449 N/(km/h)` (`0,4108 lbf/mph`)
   - `F2 = 0,0280121 N/(km/h)²` (`0,01631 lbf/mph²`)
   El ensayo EPA se realiza con peso equivalente de prueba (`ETW`) de `3.125 lb`
   (`1.417 kg`) y especificación norteamericana de `228 hp`.

4. **Tracción trasera, transferencia de carga y pérdidas (Calibrados)**:
   - Al ser de **tracción trasera (`TRASERA`)**, la aceleración longitudinal
     transfiere carga hacia el eje motriz trasero:
     ```text
     a_tracción_max = μ × (1 - fracción_delantera) × g / (1 - μ × altura_CG / batalla)
     ```
   - `factorEntregaParEfectivo = 0,80` y `adherenciaLongitudinal = 0,96` son
     parámetros **calibrados** que agrupan pérdidas mecánicas del tren motriz e
     inercia rotacional equivalente (no modelada por separado en la ecuación de
     masa puntual).
   - `aceleracionMaximaFrenadoG = 1,10` es un valor **estimado** para reproducir
     distancias de frenado de `33–35 m` desde `100 km/h`.

## Resultados de simulación y validación

Corrida con paso `dt = 0,01 s` desde parado, cambiando de marcha a `7.300 rpm`
(antes del corte de inyección a `7.400 rpm`):

| Hito de aceleración | Simulador (dt = 0,01 s) | Marcha al cruzar | Referencia publicada / medida |
| --- | ---: | :---: | --- |
| 0–50 km/h | 2,72 s | 1.ª | ~2,6–2,8 s (pruebas europeas BRZ/GR86) |
| 0–60 km/h | 3,32 s | 2.ª | — |
| 0–80 km/h | 4,70 s | 2.ª | — |
| 0–100 km/h | 6,29 s | 3.ª | **6,3 s** (Oficial Subaru Europa) |
| 0–120 km/h | 8,43 s | 3.ª | — |
| 0–130 km/h | 9,57 s | 3.ª | — |
| 0–160 km/h | 14,29 s | 4.ª | **13,8 s** (0–100 mph, *Car and Driver*); **14,5–15,0 s** (pruebas europeas) |
| 0–180 km/h | 18,69 s | 5.ª | — |
| 0–200 km/h | 24,98 s | 5.ª | **25,0–26,5 s** (pruebas europeas BRZ/GR86) |
| 0–226 km/h | 55,44 s | 6.ª | **226 km/h** (velocidad máxima oficial Subaru) |

### Observaciones físicas del modelo
- **Corte de 2.ª marcha antes de 100 km/h**: Con la relación de 2.ª (`2,188`),
  el grupo final `4,100` y el radio `0,3146 m`, el corte a `7.400 rpm` ocurre a
  `98,1 km/h`. Esto obliga a realizar el cambio `2.ª → 3.ª` justo antes de los
  `100 km/h`, reproduciendo fielmente por qué el BRZ/GR86 manual registra
  `5,4 s` en el `0–60 mph` (`96,6 km/h` en 2.ª) pero requiere `6,3 s` para el
  `0–100 km/h` en 3.ª.
- **Frenado 100–0 km/h**: El modelo se detiene en **2,42 s** y **33,6 m**,
  dentro del rango de `33–35 m` medido en pruebas con neumáticos Michelin Pilot
  Sport 4 de serie.
- **Apagado en marcha**: si se apaga el motor con una marcha engranada, la
  transmisión permanece conectada y se aplica un par de fricción y bombeo
  **estimado** del 18 % del par de la curva a las rpm mecánicas. Desde 100 km/h
  en 3.ª el modelo se detiene en unos 36 s y 420 m. El coeficiente es una
  estimación del simulador; no es una medición de Subaru.
