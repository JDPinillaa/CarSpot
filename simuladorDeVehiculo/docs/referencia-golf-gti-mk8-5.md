# Vehículo de referencia: Volkswagen Golf GTI Mk8.5

Primera referencia para reemplazar la aceleración académica por acciones
discretas con una simulación longitudinal continua. Se toma el Golf GTI
europeo actualizado en 2024, con motor 2.0 TSI de 265 PS y DSG de 7 marchas.

## Datos publicados por Volkswagen

| Dato | Valor de referencia | Fuente |
| --- | ---: | --- |
| Cilindrada | 1.984 cm³ | [Motor EA888 evo4](https://www.volkswagen-newsroom.com/en/the-new-golf-gti-and-golf-gti-clubsport-18404/the-legendary-gti-turbocharged-engine-in-two-output-levels-18408) |
| Potencia máxima | 195 kW / 265 PS, entre 5.250 y 6.500 rpm | [Comunicado de Volkswagen](https://www.volkswagen-newsroom.com/en/press-releases/volkswagen-starts-pre-sales-of-the-new-more-powerful-golf-gti-18394) |
| Par máximo | 370 Nm entre 1.600 y 4.500 rpm | [Catálogo oficial Golf 2025 de Volkswagen](https://pdf-storage.model-catalogue.vwonehub.io/publish/prod/DE/de/2025-12-03/e9d020d0b076fc1d550cae1110068fb5/der-neue-golf-2025-model-catalogue-2025-12-03.pdf) |
| Transmisión | DSG7, código DQ381-7F | [Ficha técnica Golf GTI Volkswagen Alemania](https://www.volkswagen.de/de/modelle/golf-gti.html/__layer/layers/models/der_neue_golf/vw-connect/master.layer/__ui/technische-daten.app) |
| GTI 265 PS y DQ381-7F | Confirmados juntos en el catálogo Golf 2025 de Volkswagen | [Catálogo oficial Golf 2025](https://pdf-storage.model-catalogue.vwonehub.io/publish/prod/DE/de/2026-02-09/254e8d6ecb2c77bd09b83122ee698f6a/der-neue-golf-2025-model-catalogue-2026-02-09.pdf) |
| Diseño DQ381 | Dos embragues húmedos; capacidad máxima 420 Nm | [Programa técnico VW SSP 556](https://esperformance.net/ssp/vw/SSP_556_DE.pdf) |
| DSG Golf Mk8 | DQ381-7F; VW indica que tiene modificaciones internas frente a la caja del Golf R para acomodar potencia y prestaciones distintas | [VW Self-Study Program 2022 GTI/Golf R](https://vwcup-files.track360.com/self-admin/2022-GTIandGolfR_1636751960.pdf) |
| Aceleración publicada | 0–100 km/h en 5,9 s | [Comunicado de Volkswagen](https://www.volkswagen-newsroom.com/en/press-releases/volkswagen-starts-pre-sales-of-the-new-more-powerful-golf-gti-18394) |
| Velocidad máxima | 250 km/h | [Comunicado de Volkswagen](https://www.volkswagen-newsroom.com/en/press-releases/volkswagen-starts-pre-sales-of-the-new-more-powerful-golf-gti-18394) |
| Peso mínimo en vacío | 1.457 kg | [Ficha técnica Golf GTI Volkswagen Alemania](https://www.volkswagen.de/de/modelle/golf-gti.html/__layer/layers/models/der_neue_golf/vw-connect/master.layer/__ui/technische-daten.app) |
| Neumático de 18 pulgadas | 225/40 R18 | [Guía de equipamiento Golf GTI VW 2025](https://www.volkswagen.de/idhub/content/dam/onehub_pkw/importers/ca/2024/en/showroom/buyers-guide/VW_MY25%20BG_GolfGTI_EN.pdf) |
| Batalla | 2.627 mm | [Ficha técnica Golf GTI Volkswagen Alemania](https://www.volkswagen.de/de/modelle/golf-gti.html/__layer/layers/models/der_neue_golf/vw-connect/master.layer/__ui/technische-daten.app) |
| Coeficiente aerodinámico Cd | 0,275 | [Volkswagen, Golf GTI Mk8 2020](https://www.volkswagen-newsroom.com/en/the-new-golf-gti-international-media-drive-6282/the-new-golf-gti-short-version-6283) |
| Área frontal | 2,21 m² | [Datos técnicos del Golf Mk8](https://www.volkswagen-newsroom.com/en/the-new-golf-international-vehicle-presentation-5609/the-technical-data-of-the-new-golf-5626) |

Un anuncio de despiece de un Golf GTI 2025 identifica el motor como DNPD y la
caja como WFY, pero es una fuente comercial secundaria y no confirma que ese
código aplique a todas las unidades o mercados. Debe verificarse con el VIN en
documentación Volkswagen de recambios/reparación antes de tratarlo como código
definitivo. [Anuncio de despiece (fuente secundaria)](https://en.autoplius.lt/ads/volkswagen-golf-volkswagen-golf-gti-2025m-dalimis-angliska-variklio-kodas-dn-29838684.html).

Las cifras de aceleración y velocidad máxima son objetivos de validación, no
límites artificiales de la ecuación de movimiento. El corte de velocidad a
250 km/h sí debe representarse como limitador electrónico. El peso publicado
es el mínimo indicado por Volkswagen Alemania; llantas y otros equipos pueden
cambiar la masa efectiva del vehículo concreto.

## Parámetros que requieren investigación o calibración

- Curva completa de par entre ralentí, meseta oficial de 1.600–4.500 rpm y potencia
  máxima. Volkswagen publica los puntos principales, no una tabla completa.
- Relaciones internas y finales exactas de la DQ381-7F europea. El archivo EPA
  2025 del GTI estadounidense de 241 hp declara eje 3,12 y N/V 26,6; [EPA define
  N/V](https://nepis.epa.gov/Exe/ZyPURL.cgi?Dockey=P1004EZ7.TXT) como rpm del
  motor por mph en la marcha más alta. Con el radio geométrico de 0,319 m, eso
  implica una relación global de 1,984 y 7.ª 0,635 si se divide por 3,125. Es
  una derivación para la variante estadounidense,
  usada como proxy, no una cifra de caja publicada para el GTI europeo. El SSP
  556 describe dos trenes de engranajes y no da sus reducciones para esta
  aplicación. El perfil usa relaciones de un León 2.0 TSI DQ381-7F FWD como
  proxy para 1.ª–6.ª, y conserva 7.ª 0,635 por el N/V del GTI.
- Hay tablas de fabricante para otras aplicaciones DQ381-7F, pero sus relaciones
  no son intercambiables sin confirmar la referencia de caja. El catálogo
  técnico del SEAT León 2018 publica para el 2.0 TSI 190 DSG-7 (DQ381-7F)
  3,400 / 2,750 / 1,767 / 0,925 / 0,795 / 0,755 / 0,635 y grupos 4,167 / 3,125.
  Para el 2.0 TSI 290 con DQ381-7F + VAQ publica
  3,190 / 2,750 / 1,897 / 1,040 / 0,793 / 0,860 / 0,661 y grupos
  4,471 / 3,304. Fuente: [catálogo técnico oficial SEAT León](https://www.seat.com.mt/content/dam/public/seat-website/car-shopping-tools/brochure-download/brochures/leon/cars-specs-brochure-5F1-NA-december-2018.pdf).
  La diferencia entre aplicaciones muestra que “DQ381-7F” por sí solo no
  identifica una relación única. Además, el programa técnico VW del Golf Mk8
  indica que la DSG del GTI incorpora modificaciones internas respecto a la
  del Golf R; por eso no usamos ratios del R como si fueran los del GTI. Como
  proxy inicial, el perfil toma las
  relaciones del León 190 FWD y el final 3,125, y asigna 4,167 a las marchas
  1/4/5 y 3,125 a 2/3/6/7 según esta [referencia técnica secundaria de DQ381](https://www.motor-talk.de/forum/getriebeuebersetzungen-t5955364.html?page=10).
  El N/V EPA del GTI confirma la combinación de 7.ª 0,635 × final 3,125, pero
  no confirma el grupo 4,167 ni el mapa exacto por marchas del GTI Mk8.5. El
  perfil mantiene esa incertidumbre explícita hasta obtener el código de caja
  o datos de reparación/recambios Volkswagen para la variante exacta.
  Como corroboración parcial, el certificado suizo de tipo de un Golf GTI
  anterior (DNPA, 180 kW) informa finales 4,17/3,13 para la variante DSG:
  [certificado de tipo](https://www.dauto.ch/de/typenscheine/1xz551-vw-golf-8-2-0-gti).
  Esto apoya esos dos valores en una aplicación GTI cercana, pero no publica
  relaciones por marcha ni demuestra que el GTI Mk8.5 DNPD/WFY use la misma
  asignación. Los anuncios localizados para WFY tampoco aportan una tabla fiable
  de relaciones internas. Se mantienen, por tanto, los ratios actuales como
  proxies y no se cambia el perfil por esta evidencia.
- VW publica Cd 0,275 y área 2,21 m² para el Golf Mk8, de donde se deriva CdA
  0,608 m². La EPA publica en su [lista de autos de prueba 2025](https://www.epa.gov/system/files/documents/2026-01/25-testcar-2026-01-21.xlsx)
  coeficientes "Set Coef A/B/C" para un Golf GTI estadounidense de 241 hp,
  tracción delantera y DSG de 7 marchas: 14,01 lbf, 0,1194 lbf/mph y
  0,01828 lbf/mph². El perfil convierte esa curva a F0=62,32 N,
  F1=0,33002 N/(km/h) y F2=0,0313953 N/(km/h)². Es una referencia de ensayo
  cercana, pero no corresponde al GTI europeo de 265 PS; la EPA indica 3.500 lb
  (1.588 kg) de peso de prueba frente a los 1.457 kg mínimos del perfil, además
  de diferir el mercado. Los coeficientes EPA incluyen el conjunto de
  resistencias del vehículo y no deben interpretarse como CdA puro.
- Reparto de peso, altura del centro de gravedad y adherencia son estimaciones;
  el reparto de carga sigue siendo una aproximación. AUTO BILD publica 64/36
  delante/detrás para su unidad de prueba, una referencia medida más específica
  que el 0,62 provisional del perfil; no se ha trasladado aún porque su coche
  llevaba ruedas de 19 pulgadas y los demás parámetros no están igualmente
  identificados.

Mientras no haya datos públicos fiables del código exacto, esos valores son
proxies calibrados para aproximar mediciones publicadas. El perfil usa finales
por marcha: 4,167 en 1.ª/4.ª/5.ª y 3,125 en 2.ª/3.ª/6.ª/7.ª, mapeo de trenes
de una aplicación DQ381-7F cercana. El final 3,125 y 7.ª 0,635 reproducen el
N/V EPA; falta confirmar el grupo 4,167 y la asignación de trenes del GTI
europeo. La coincidencia de aceleración no valida independientemente la caja.
El radio geométrico calculado de 0,319 m deriva de
225/40 R18; la curva de resistencia también usa datos EPA como referencia.
El factor efectivo de entrega de par (0,81) y μ=1,11 se calibraron contra aceleraciones
instrumentadas; el primero agrega pérdidas y otros efectos del tren motriz, y
no es una medición de eficiencia de la DQ381. μ tampoco es una medición del
neumático. También se
estima reparto estático delantero de 0,62 y altura del
centro de gravedad de 0,55 m; la batalla de 2,627 m sí está publicada por VW.
La fuerza del eje motriz se limita con una aproximación de transferencia de carga:

```text
a_tracción_max ≈ μ × fracción_delantera × g / (1 + μ × altura_CG / batalla)
F_tracción_max = masa × a_tracción_max
```

El reparto, altura y adherencia se han calibrado juntos para el eje delantero;
no son valores de catálogo. El radio geométrico no es el radio dinámico bajo
carga; queda pendiente medirlo o documentarlo. La desaceleración máxima del
perfil (1,05 g) también es un parámetro estimado, no una cifra de Volkswagen.

## Modelo longitudinal

El avance se calcula con unidades SI y pasos de tiempo pequeños:

```text
F_rueda = par_motor(rpm) × relación_marcha × relación_final × factor_entrega / radio_rueda
F_res   = F0 + F1 × velocidad_kmh + F2 × velocidad_kmh²
a       = (F_rueda - F_res - F_freno - F_motor_frenado) / masa
```

Las rpm salen de la velocidad de rueda y las relaciones de transmisión. La DSG
selecciona marchas según carga y rpm, hace kickdown ante una demanda alta y
retiene parte del par durante el cambio para representar la transferencia entre
embragues. El limitador actúa a 250 km/h; los pedales mantienen demanda continua.
El mapa de cambios también es estimado: sube de marcha cerca de 1.600 rpm con
carga baja, 2.600 rpm a carga media-baja, 4.200 rpm a carga media y 6.350 rpm
cerca de plena carga. En la simulación, al levantar el acelerador a 100 km/h,
la caja pasa de tercera a quinta en unos 0,3 s y a séptima en unos 0,5 s
—cuatro cambios consecutivos, con 0,16 s de retardo entre ellos—, y estabiliza
cerca de 1.635 rpm a 98,8 km/h, coherente por construcción con el N/V usado
como referencia; no es una medición del mapa de cambios del fabricante.

Al apagar el motor con una marcha engranada, el modelo mantiene la transmisión
conectada y aplica un par de fricción y bombeo **estimado** del 18 % del par de
la curva a las rpm mecánicas, sin combustión. Con ese supuesto, el Golf apagado
a 100 km/h en 3.ª se detiene en unos 38 s y 420 m. El coeficiente no es un dato
de Volkswagen; para simular punto muerto o embrague desacoplado haría falta
modelar esa desconexión.

## Implementación y calibración inicial

El perfil `golf-gti-mk8-5` ya usa el integrador longitudinal, curva de par
interpolada, resistencias, limitador electrónico y DQ381-7F. En cada cambio se
retiene 75% de la fuerza durante 0,16 s (estimaciones de transferencia de par y
duración); un kickdown de varias marchas acumula el retardo. Acelerador y freno
funcionan al mantener presionados los botones o las teclas `W`/`S`, y se liberan
al perder el foco de la ventana. Con la curva de par y los parámetros actuales,
la corrida da:

La curva de par conserva exactamente 195 kW entre 5.250 y 6.500 rpm: los puntos
de par intermedios se derivan de `T = P / ω` y evitan que una interpolación
lineal simple cree potencia adicional no publicada. El par medido/publicado
dentro de esa meseta no está disponible como tabla completa.

| Medición desde parado, acelerador a fondo | Modelo, paso 0,01 s | Referencia |
| --- | ---: | --- |
| 0–50 km/h | 2,57 s | 2,5 s, AUTO BILD |
| 0–60 km/h | 3,13 s | 3,01 s, Autocar India |
| 0–80 km/h | 4,21 s | 4,16 s, Autocar India |
| 0–100 km/h | 5,75 s | 5,9 s, Volkswagen; 5,8 s, AUTO BILD; 5,73 s, Autocar India |
| 0–120 km/h | 7,54 s | 7,58 s, Autocar India |
| 0–130 km/h | 8,56 s | 8,8 s, AUTO BILD |
| 0–160 km/h | 12,74 s | 12,9 s, AUTO BILD |
| 0–180 km/h | 16,25 s | Sin cifra comparable |
| 0–200 km/h | 21,03 s | 21,3 s, AUTO BILD |
| 0–250 km/h | 76,56 s | Predicción hasta el limitador; VW publica vmax, no tiempo 0–250 |

La corrida a 0,01 s es la referencia actual del código. Al repetir con 0,02 s,
las marcas de velocidad difieren hasta 0,02 s en este tramo. Con los grupos
finales aproximados, la transmisión alcanza 250 km/h en sexta a unas 4.905 rpm;
la séptima derivada del N/V queda como marcha de crucero. 0–250 es sensible a
relaciones, pérdidas y resistencia, y carece de una medición comparable.

Para aislar el efecto de la configuración publicada por AUTO BILD, se hizo una
segunda corrida cambiando solo masa a 1.455 kg, radio geométrico a 0,32355 m
(235/35 R19) y reparto delantero a 0,64. Se dejaron iguales la curva de
resistencia EPA, μ=1,11, altura de CG, curva motor y relaciones proxy:

| Hito | Perfil base R18 | Parámetros de unidad AUTO BILD | AUTO BILD |
| --- | ---: | ---: | ---: |
| 0–50 km/h | 2,57 s | 2,49 s | 2,5 s |
| 0–100 km/h | 5,75 s | 5,65 s | 5,8 s |
| 0–130 km/h | 8,56 s | 8,48 s | 8,8 s |
| 0–160 km/h | 12,74 s | 12,66 s | 12,9 s |
| 0–200 km/h | 21,03 s | 20,87 s | 21,3 s |

La segunda corrida se aproxima al dato de 0–50, pero resulta 0,15 s más rápida
en 0–100 y 0,43 s en 0–200. No es una calibración independiente: mezcla tres
parámetros de la unidad de prueba con la resistencia EPA estadounidense y
estimaciones sin medir. No se reajusta el factor de par para encubrir esa
diferencia; hacen falta resistencia de carretera y relaciones verificadas del
mismo coche de prueba.

En la configuración R18 base, 0–100 queda 0,02 s por encima de Autocar India,
0,05 s por debajo de AUTO BILD y 0,15 s por debajo de la cifra de Volkswagen;
0–160 y 0–200 quedan 0,16 s y 0,27 s por debajo de AUTO BILD. Los tiempos del
modelo son predicciones, no cifras de fábrica. Sí existen
pruebas instrumentadas independientes: [Autocar India](https://www.autocarindia.com/auto-features-amp/2025-volkswagen-golf-gti-performance-tested-438881)
midió 5,73 s de 0–100 y 7,58 s de 0–120 con un GTI 2025 de 265 hp,
225/40 R18 y 1.454 kg declarados; [AUTO BILD](https://www.autobild.de/artikel/vw-golf-gti-2.0-tsi-dsg-facelift-test-27788887.html)
reportó 2,5 s a 50, 5,8 s a 100, 8,8 s a 130, 12,9 s a 160 y 21,3 s a 200
con un Mk8.5 de 19 pulgadas, neumáticos 235/35 R19, 1.455 kg y reparto medido
64/36. El perfil de partida conserva la configuración de 18 pulgadas y 62/38
estimado; por eso las comparaciones cuantifican desviación, pero no aíslan
errores de relaciones, resistencia, tracción, masa o neumático. Las cifras son
referencias de prueba, no especificaciones oficiales y las condiciones no son
idénticas.

El factor de entrega 0,81, el par estimado de 270 Nm a 850 rpm y μ=1,11 se
calibraron inicialmente contra aceleraciones publicadas. No se reajusta un
factor global para ocultar diferencias que pueden venir de los ratios proxy,
del GTI EPA estadounidense o de condiciones de ensayo. Estas cifras son
calibración empírica del conjunto, no prueban que las pérdidas o adherencia
sean las de fábrica.

Si se quita el limitador electrónico solo para analizar la capacidad del tren
motriz, una simulación de 300 s se acerca a 253 km/h en sexta y acelera muy
despacio por encima de 240 km/h. Es una predicción de baja confianza, muy
sensible al factor de entrega y a la resistencia EPA proxy; no es velocidad
publicada ni meta del perfil. El Golf de referencia está limitado a 250 km/h.

La curva interpola puntos oficiales y estima zonas no publicadas. Al frenar a
fondo desde 100 km/h, el modelo recorre unos 36,2 m; AUTO BILD midió 34,6 m en
frío y 33,4 m en caliente con neumáticos 235/35 R19. Desde 80 km/h el modelo da
23,2 m, frente a 26,37 m de Autocar India; las pruebas difieren en ruedas y
condiciones y muestran que una sola desaceleración constante no reproduce
todas las mediciones. Desde 250 km/h la predicción es 6,31 s y 214,9 m,
sin dato comparable encontrado. Aún no se
modelan inclinación, viento, temperatura, variación de adherencia, inercia
rotacional, control de lanzamiento ni degradación térmica de frenos. La fórmula
de transferencia de carga es una aproximación longitudinal, no sustituye un
modelo completo de neumáticos o suspensión.

## Próximos pasos

1. Obtener las relaciones por marcha y los finales de los dos trenes de la
   DQ381-7F europea, además del radio dinámico, con fuentes del mercado exacto.
   Incorporar esos datos al perfil `GolfGtiMk85` cuando haya fuentes verificables.
2. Explicar y reducir la desviación de 0–160/0–200 frente a pruebas
   instrumentadas comparables; primero reproducir la configuración de prueba
   (masa/ruedas/reparto conocidos) antes de calibrar parámetros globales.
3. Mejorar el frenado para representar variación de agarre, ABS y carga sobre
   cada eje; contrastar con mediciones comparables.
4. Separar el modelo genérico de los perfiles ficticios heredados.
