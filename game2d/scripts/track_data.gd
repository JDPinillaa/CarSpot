class_name TrackData
extends RefCounted

## Definiciones de circuitos. Todo es dato: para añadir una pista basta con
## agregar un diccionario a TRACKS. El trazado se interpola con Catmull-Rom y
## la malla, los bordes y los puntos de control se generan en tiempo de carga.
##
## Coordenadas en metros. La línea de meta está en el primer punto y el coche
## arranca mirando hacia el segundo. En las tres pistas la meta cae sobre una
## recta: los puntos anteriores y posteriores están casi alineados con ella,
## así el auto no entra torcido al cronómetro.

const TRACKS := [
	{
		"name": "Circuito Norte",
		"subtitle": "Trazado mixto, 10 m de ancho",
		"width": 10.0,
		"points": [
			Vector2(-12, 30), Vector2(-12, 0), Vector2(-12, -30), Vector2(-4, -58),
			Vector2(20, -78), Vector2(50, -81), Vector2(68, -59), Vector2(65, -27),
			Vector2(43, -14), Vector2(41, 14), Vector2(59, 32), Vector2(54, 59),
			Vector2(27, 72), Vector2(4, 64), Vector2(-5, 48),
		],
		"theme": {
			"grass": Color(0.17, 0.26, 0.15),
			"grass_alt": Color(0.21, 0.31, 0.17),
			"asphalt": Color(0.20, 0.21, 0.24),
			"asphalt_edge": Color(0.26, 0.27, 0.30),
			"curb_a": Color(0.78, 0.16, 0.16),
			"curb_b": Color(0.90, 0.90, 0.88),
			"line": Color(0.82, 0.83, 0.80),
			"sky": Color(0.10, 0.15, 0.09),
		},
	},
	{
		"name": "Óvalo Costero",
		"subtitle": "Rápido, 12 m de ancho",
		"width": 12.0,
		"points": [
			Vector2(0, -55), Vector2(20, -55), Vector2(55, -38), Vector2(70, 0),
			Vector2(55, 38), Vector2(20, 55), Vector2(0, 55), Vector2(-20, 55),
			Vector2(-55, 38), Vector2(-70, 0), Vector2(-55, -38), Vector2(-20, -55),
		],
		"theme": {
			"grass": Color(0.22, 0.24, 0.16),
			"grass_alt": Color(0.27, 0.29, 0.19),
			"asphalt": Color(0.22, 0.22, 0.24),
			"asphalt_edge": Color(0.29, 0.29, 0.31),
			"curb_a": Color(0.14, 0.36, 0.72),
			"curb_b": Color(0.92, 0.92, 0.90),
			"line": Color(0.85, 0.86, 0.84),
			"sky": Color(0.12, 0.18, 0.10),
		},
	},
	{
		"name": "Serpiente Técnica",
		"subtitle": "Curvas cerradas, 9 m de ancho",
		"width": 9.0,
		"points": [
			Vector2(8, 60), Vector2(34, 56), Vector2(54, 42), Vector2(58, 20),
			Vector2(44, 6), Vector2(22, 8), Vector2(10, -8), Vector2(22, -26),
			Vector2(46, -32), Vector2(58, -48), Vector2(44, -66), Vector2(18, -68),
			Vector2(-2, -56), Vector2(-18, -40), Vector2(-38, -44), Vector2(-52, -60),
			Vector2(-68, -52), Vector2(-72, -30), Vector2(-58, -16), Vector2(-38, -12),
			Vector2(-26, 2), Vector2(-34, 20), Vector2(-54, 28), Vector2(-64, 44),
			Vector2(-46, 58), Vector2(-20, 60),
		],
		"theme": {
			"grass": Color(0.15, 0.22, 0.18),
			"grass_alt": Color(0.19, 0.27, 0.21),
			"asphalt": Color(0.19, 0.20, 0.22),
			"asphalt_edge": Color(0.25, 0.26, 0.29),
			"curb_a": Color(0.85, 0.55, 0.10),
			"curb_b": Color(0.92, 0.92, 0.90),
			"line": Color(0.85, 0.86, 0.82),
			"sky": Color(0.09, 0.14, 0.11),
		},
	},
]


static func count() -> int:
	return TRACKS.size()


static func get_track(index: int) -> Dictionary:
	return TRACKS[clampi(index, 0, TRACKS.size() - 1)]
