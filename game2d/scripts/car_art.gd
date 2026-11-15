class_name CarArt
extends RefCounted

## Dibujo del coche visto desde arriba, 100 % por código.
##
## Se usa tanto para el coche del jugador como para el fantasma, así que no
## depende de nodos concretos: recibe un CanvasItem sobre el que dibujar.
## El morro apunta hacia +X local.

const BODY := [
	Vector2(1.00, 0.00), Vector2(0.97, 0.38), Vector2(0.88, 0.68),
	Vector2(0.70, 0.88), Vector2(0.42, 0.98), Vector2(0.05, 1.00),
	Vector2(-0.32, 0.99), Vector2(-0.62, 0.94), Vector2(-0.82, 0.80),
	Vector2(-0.93, 0.58), Vector2(-0.98, 0.30), Vector2(-1.00, 0.00),
	Vector2(-0.98, -0.30), Vector2(-0.93, -0.58), Vector2(-0.82, -0.80),
	Vector2(-0.62, -0.94), Vector2(-0.32, -0.99), Vector2(0.05, -1.00),
	Vector2(0.42, -0.98), Vector2(0.70, -0.88), Vector2(0.88, -0.68),
	Vector2(0.97, -0.38),
]

const COCKPIT := [
	Vector2(-0.56, -0.52), Vector2(-0.30, -0.60), Vector2(0.26, -0.53),
	Vector2(0.47, -0.30), Vector2(0.47, 0.30), Vector2(0.26, 0.53),
	Vector2(-0.30, 0.60), Vector2(-0.56, 0.52),
]

const ROOF := [
	Vector2(-0.34, -0.44), Vector2(-0.10, -0.50), Vector2(0.20, -0.42),
	Vector2(0.28, -0.26), Vector2(0.28, 0.26), Vector2(0.20, 0.42),
	Vector2(-0.10, 0.50), Vector2(-0.34, 0.44),
]


static func draw_car(canvas: CanvasItem, spec: Dictionary, steer: float, brake_light: float, alpha: float = 1.0) -> void:
	var hl: float = float(spec["length"]) * 0.5
	var hw: float = float(spec["width"]) * 0.5
	var lf: float = float(spec["cg_front"])
	var lr: float = float(spec["wheelbase"]) - lf
	var tw: float = float(spec["track_width"]) * 0.5
	var body: Color = spec["color"]
	var glass: Color = spec["glass"]
	var accent: Color = spec["accent"]

	var simple := alpha < 0.99

	# Sombra proyectada.
	var shadow := PackedVector2Array()
	for n in BODY:
		shadow.append(Vector2(n.x * hl + 0.28, n.y * hw + 0.42))
	canvas.draw_colored_polygon(shadow, Color(0.0, 0.0, 0.0, 0.30 * alpha))

	# Ruedas (rectángulos rotados a mano para no tocar la transformada).
	var wheel_len := 0.66
	var wheel_w := 0.30
	var tire := Color(0.055, 0.055, 0.065, alpha)
	var rim := Color(0.22, 0.23, 0.25, alpha)
	for axle_value in [lf, -lr]:
		var axle := float(axle_value)
		for side_value in [-1.0, 1.0]:
			var side := float(side_value)
			var angle := steer if axle > 0.0 else 0.0
			var center := Vector2(axle, side * tw)
			canvas.draw_colored_polygon(_rot_rect(center, angle, wheel_len * 0.5, wheel_w * 0.5), tire)
			canvas.draw_colored_polygon(_rot_rect(center, angle, wheel_len * 0.28, wheel_w * 0.28), rim)

	if simple:
		var ghost_pts := PackedVector2Array()
		for n in BODY:
			ghost_pts.append(Vector2(n.x * hl, n.y * hw))
		canvas.draw_colored_polygon(ghost_pts, Color(body.r, body.g, body.b, 0.30 * alpha))
		canvas.draw_polyline(_closed(ghost_pts), Color(accent.r, accent.g, accent.b, 0.55 * alpha), 0.16)
		canvas.draw_circle(Vector2(0.1 * hl, 0.0), 0.18, Color(1, 1, 1, 0.55 * alpha))
		return

	var body_pts := PackedVector2Array()
	for n in BODY:
		body_pts.append(Vector2(n.x * hl, n.y * hw))

	# Carrocería con dos capas para dar volumen.
	canvas.draw_colored_polygon(body_pts, body)
	var inner := PackedVector2Array()
	for n in BODY:
		inner.append(Vector2(n.x * hl * 0.985, n.y * hw * 0.96))
	canvas.draw_colored_polygon(inner, body.lightened(0.10))
	canvas.draw_polyline(_closed(body_pts), body.darkened(0.45), 0.10)

	# Franjas deportivas.
	var stripe_y := hw * 0.25
	var stripe_w := maxf(0.06, hw * 0.10)
	canvas.draw_line(Vector2(-hl * 0.82, stripe_y), Vector2(hl * 0.86, stripe_y), accent, stripe_w)
	canvas.draw_line(Vector2(-hl * 0.82, -stripe_y), Vector2(hl * 0.86, -stripe_y), accent, stripe_w)

	# Alerón trasero.
	canvas.draw_rect(Rect2(-hl * 1.0, -hw * 0.80, hl * 0.13, hw * 1.60), body.darkened(0.55))
	canvas.draw_rect(Rect2(-hl * 1.03, -hw * 0.86, hl * 0.05, hw * 1.72), accent.darkened(0.10))

	# Habitáculo / cristales.
	var cockpit := PackedVector2Array()
	for n in COCKPIT:
		cockpit.append(Vector2(n.x * hl, n.y * hw))
	canvas.draw_colored_polygon(cockpit, glass)

	var roof := PackedVector2Array()
	for n in ROOF:
		roof.append(Vector2(n.x * hl, n.y * hw))
	canvas.draw_colored_polygon(roof, body.darkened(0.18))

	# Parabrisas.
	var windshield := PackedVector2Array([
		Vector2(0.26 * hl, -0.50 * hw), Vector2(0.47 * hl, -0.30 * hw),
		Vector2(0.47 * hl, 0.30 * hw), Vector2(0.26 * hl, 0.50 * hw),
	])
	canvas.draw_colored_polygon(windshield, glass.lightened(0.20))

	# Espejos.
	canvas.draw_rect(Rect2(0.18 * hl, -hw - 0.16, 0.26, 0.14), body.darkened(0.25))
	canvas.draw_rect(Rect2(0.18 * hl, hw + 0.02, 0.26, 0.14), body.darkened(0.25))

	# Faros y pilotos.
	for side in [-1.0, 1.0]:
		canvas.draw_circle(Vector2(hl * 0.93, side * hw * 0.56), 0.14, Color(1.0, 0.96, 0.80))
		var tail := Color(0.95, 0.10, 0.10).lerp(Color(1.0, 0.55, 0.40), brake_light)
		canvas.draw_circle(Vector2(-hl * 0.985, side * hw * 0.56), 0.15, tail)
		if brake_light > 0.05:
			canvas.draw_circle(Vector2(-hl * 0.985, side * hw * 0.56), 0.38, Color(1.0, 0.12, 0.08, 0.20 * brake_light))


## Dibuja el coche en miniatura para el menú.
static func draw_mini(canvas: CanvasItem, spec: Dictionary, scale: float,
		origin: Vector2 = Vector2.ZERO, angle: float = -PI * 0.5) -> void:
	canvas.draw_set_transform(origin, angle, Vector2(scale, scale))
	draw_car(canvas, spec, 0.0, 0.0, 1.0)
	canvas.draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)


static func _rot_rect(center: Vector2, angle: float, half_len: float, half_w: float) -> PackedVector2Array:
	var f := Vector2.from_angle(angle)
	var r := Vector2(-f.y, f.x)
	var out := PackedVector2Array()
	out.append(center - f * half_len - r * half_w)
	out.append(center + f * half_len - r * half_w)
	out.append(center + f * half_len + r * half_w)
	out.append(center - f * half_len + r * half_w)
	return out


static func _closed(points: PackedVector2Array) -> PackedVector2Array:
	var out := points.duplicate()
	out.append(points[0])
	return out
