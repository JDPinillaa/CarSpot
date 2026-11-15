class_name CarSpecs
extends RefCounted

## Especificaciones de los coches. La física, el HUD y el dibujo leen de aquí:
## cambiar masa, neumáticos o curva de par cambia el comportamiento sin tocar
## una línea de simulación. Todos los valores están en unidades SI.

const CARS := [
	{
		"name": "GT Ligero",
		"subtitle": "Deportivo equilibrado",
		"blurb": "Peso bajo y reparto neutro. Perdona los errores y premia la trazada limpia.",
		"mass": 1080.0,
		"wheelbase": 2.55,
		"cg_front": 1.22,
		"length": 4.25,
		"width": 1.86,
		"track_width": 1.62,
		"max_steer_deg": 32.0,
		"steer_rate": 5.2,
		"grip_front": 1.20,
		"grip_rear": 1.52,
		"brake_force": 13500.0,
		"reverse_force": 3000.0,
		"drag_area": 0.60,
		"downforce_area": 2.8,
		"torque_curve": [
			Vector2(800, 150), Vector2(2000, 250), Vector2(3500, 310),
			Vector2(4600, 330), Vector2(5600, 314), Vector2(6600, 276),
			Vector2(7800, 210),
		],
		"gear_ratios": [3.40, 2.25, 1.65, 1.28, 1.02, 0.84],
		"final_drive": 3.90,
		"shift_up": 6900.0,
		"shift_down": 2500.0,
		"limiter": 7800.0,
		"launch_rpm": 2600.0,
		"color": Color(0.88, 0.16, 0.20),
		"accent": Color(0.98, 0.98, 0.96),
		"glass": Color(0.10, 0.16, 0.22),
	},
	{
		"name": "Muscle V8",
		"subtitle": "Potencia y derrape",
		"blurb": "Mucho par y poco agarre trasero. Frena antes y dosifica el gas a la salida.",
		"mass": 1520.0,
		"wheelbase": 2.85,
		"cg_front": 1.42,
		"length": 4.90,
		"width": 2.00,
		"track_width": 1.66,
		"max_steer_deg": 30.0,
		"steer_rate": 4.3,
		"grip_front": 1.08,
		"grip_rear": 1.24,
		"brake_force": 14500.0,
		"reverse_force": 3400.0,
		"drag_area": 0.78,
		"downforce_area": 1.7,
		"torque_curve": [
			Vector2(800, 300), Vector2(1800, 430), Vector2(3200, 510),
			Vector2(4300, 520), Vector2(5200, 470), Vector2(6500, 380),
		],
		"gear_ratios": [3.05, 1.95, 1.42, 1.08, 0.86],
		"final_drive": 3.55,
		"shift_up": 5700.0,
		"shift_down": 2200.0,
		"limiter": 6500.0,
		"launch_rpm": 2200.0,
		"color": Color(0.16, 0.38, 0.82),
		"accent": Color(0.96, 0.74, 0.16),
		"glass": Color(0.08, 0.12, 0.18),
	},
	{
		"name": "Fórmula Kart",
		"subtitle": "Ágil y nervioso",
		"blurb": "Poco peso y muchísimo agarre. Gira donde otros frenan; castiga el exceso de volante.",
		"mass": 570.0,
		"wheelbase": 1.78,
		"cg_front": 0.90,
		"length": 3.05,
		"width": 1.54,
		"track_width": 1.34,
		"max_steer_deg": 38.0,
		"steer_rate": 7.0,
		"grip_front": 1.42,
		"grip_rear": 1.64,
		"brake_force": 9500.0,
		"reverse_force": 2200.0,
		"drag_area": 0.44,
		"downforce_area": 4.2,
		"torque_curve": [
			Vector2(1200, 60), Vector2(3000, 95), Vector2(5000, 118),
			Vector2(6500, 128), Vector2(8200, 120), Vector2(9200, 100),
		],
		"gear_ratios": [2.90, 2.10, 1.62, 1.28, 1.02, 0.84],
		"final_drive": 3.20,
		"shift_up": 8200.0,
		"shift_down": 3400.0,
		"limiter": 9200.0,
		"launch_rpm": 3200.0,
		"color": Color(0.98, 0.72, 0.10),
		"accent": Color(0.10, 0.12, 0.14),
		"glass": Color(0.12, 0.16, 0.20),
	},
]


static func count() -> int:
	return CARS.size()


static func get_car(index: int) -> Dictionary:
	return CARS[clampi(index, 0, CARS.size() - 1)]


## Potencia aproximada en CV, para mostrar en el menú.
static func horsepower(spec: Dictionary) -> int:
	var best := 0.0
	for point in spec["torque_curve"]:
		best = maxf(best, point.x * point.y * TAU / 60.0)
	return int(best / 735.5)


static func weight_power(spec: Dictionary) -> float:
	return float(spec["mass"]) / maxf(1.0, float(horsepower(spec)))
