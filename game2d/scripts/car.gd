class_name RaceCar
extends Node2D

## Vehículo con modelo dinámico de bicicleta.
##
## Estado: posición, velocidad mundial, rumbo y velocidad de guiñada. En cada
## subpaso se calculan los ángulos de deslizamiento de ambos ejes, las fuerzas
## laterales (saturadas por el círculo de fricción) y la fuerza longitudinal
## que entrega Powertrain. Nada de esto usa nodos físicos de Godot: es
## integración propia, lo que permite ajustar el coche con datos.
##
## Ejes locales: +X = morro, +Y = derecha. Giro positivo = hacia la derecha.

const SUBSTEPS := 4
const RHO := 1.2

var spec: Dictionary
var powertrain: Powertrain
var track: RaceTrack

var velocity := Vector2.ZERO
var heading := 0.0
var yaw_rate := 0.0
var steer := 0.0

var throttle := 0.0
var brake := 0.0
var steer_input := 0.0
var handbrake := false
var input_enabled := true
var active := true

var speed_kmh := 0.0
var slip := 0.0
var wheelspin := 0.0
var on_track := true
var surface_grip := 1.0
var lateral_g := 0.0
var longitudinal_g := 0.0
var brake_light := 0.0
var track_index := 0
var shift_flash := 0.0
var dbg := {}

var _smoke: Array = []
var _last_gear := 1
static var _smoke_texture: ImageTexture


func setup(car_spec: Dictionary, race_track: RaceTrack) -> void:
	spec = car_spec
	track = race_track
	powertrain = Powertrain.new()
	powertrain.configure(spec)
	z_index = 10

	for i in 2:
		var particles := _make_smoke()
		particles.position = Vector2(-(float(spec["wheelbase"]) - float(spec["cg_front"])),
			(-1.0 if i == 0 else 1.0) * float(spec["track_width"]) * 0.5)
		add_child(particles)
		_smoke.append(particles)

	reset_at(track.start_position(), track.start_heading())


func reset_at(pos: Vector2, angle: float) -> void:
	global_position = pos
	heading = angle
	rotation = angle
	velocity = Vector2.ZERO
	yaw_rate = 0.0
	steer = 0.0
	steer_input = 0.0
	throttle = 0.0
	brake = 0.0
	handbrake = false
	slip = 0.0
	wheelspin = 0.0
	brake_light = 0.0
	speed_kmh = 0.0
	shift_flash = 0.0
	track_index = track.nearest_index(pos)
	if powertrain != null:
		powertrain.reset()
	queue_redraw()


func rear_wheel_positions() -> Array:
	var lr := float(spec["wheelbase"]) - float(spec["cg_front"])
	var tw := float(spec["track_width"]) * 0.5
	return [to_global(Vector2(-lr, -tw)), to_global(Vector2(-lr, tw))]


func telemetry() -> Dictionary:
	return {
		"speed_kmh": speed_kmh,
		"rpm": powertrain.rpm if powertrain != null else 0.0,
		"gear": powertrain.gear if powertrain != null else 1,
		"throttle": throttle,
		"brake": brake,
		"steer": steer_input,
		"slip": slip,
		"wheelspin": wheelspin,
		"on_track": on_track,
		"lateral_g": lateral_g,
		"longitudinal_g": longitudinal_g,
		"limiter": powertrain.limiter_active if powertrain != null else false,
		"shift": shift_flash,
	}


func _physics_process(delta: float) -> void:
	if not active or spec.is_empty():
		return

	if input_enabled:
		throttle = Input.get_action_strength("throttle")
		brake = Input.get_action_strength("brake")
		steer_input = Input.get_action_strength("steer_right") - Input.get_action_strength("steer_left")
		handbrake = Input.is_action_pressed("handbrake")

	# El volante tiene recorrido limitado y se endurece con la velocidad.
	var speed_now := velocity.length()
	var speed_factor := clampf(speed_now / 42.0, 0.0, 1.0)
	var max_steer := deg_to_rad(float(spec["max_steer_deg"])) * lerpf(1.0, 0.34, speed_factor)
	steer = move_toward(steer, steer_input * max_steer, float(spec["steer_rate"]) * delta)

	for i in SUBSTEPS:
		_integrate(delta / float(SUBSTEPS))

	global_position += velocity * delta
	heading = wrapf(heading + yaw_rate * delta, -PI, PI)
	rotation = heading

	if powertrain != null:
		if powertrain.gear != _last_gear:
			_last_gear = powertrain.gear
			shift_flash = 1.0
		shift_flash = maxf(0.0, shift_flash - delta * 3.0)

	_update_effects(delta)
	queue_redraw()


func _integrate(dt: float) -> void:
	var fwd := Vector2.from_angle(heading)
	var right := Vector2(-fwd.y, fwd.x)
	var vx := velocity.dot(fwd)
	var vy := velocity.dot(right)
	var speed := velocity.length()

	track_index = track.nearest_index(global_position, track_index)
	surface_grip = track.surface_grip(global_position, track_index)
	on_track = surface_grip > 0.9

	var m: float = spec["mass"]
	var wb: float = spec["wheelbase"]
	var lf: float = spec["cg_front"]
	var lr: float = wb - lf
	var iz: float = m * wb * wb * 0.30
	var g := 9.81
	var grip_f: float = float(spec["grip_front"]) * surface_grip
	var grip_r: float = float(spec["grip_rear"]) * surface_grip

	# Carga aerodinámica: más peso en los neumáticos a alta velocidad.
	var speed_sq := speed * speed
	var downforce := 0.5 * RHO * float(spec["downforce_area"]) * speed_sq
	var fz_f := m * g * lr / wb + downforce * 0.45
	var fz_r := m * g * lf / wb + downforce * 0.55
	var grip_force_f := grip_f * fz_f
	var grip_force_r := grip_r * fz_r
	var stiffness_f := grip_force_f / 0.11
	var stiffness_r := grip_force_r / 0.13

	# Motor y frenos.
	var wheel_omega := vx / Powertrain.WHEEL_RADIUS
	var demand := powertrain.update(dt, wheel_omega, throttle)
	# Control de tracción suave: a baja velocidad el neumático no puede
	# convertir todo el par en avance, así que se limita la demanda. Sin esto
	# el coche queda con cero agarre lateral en cada salida.
	var tc := lerpf(0.72, 1.0, clampf(absf(vx) / 30.0, 0.0, 1.0))
	var traction_cap := grip_force_r * tc
	var traction := 0.0
	if vx > -0.6:
		traction = clampf(demand, -traction_cap, traction_cap)
	elif throttle > 0.05:
		# Rodando hacia atrás con el acelerador: primero hay que detenerla.
		traction = throttle * float(spec["brake_force"]) * 0.75

	var brake_force := 0.0
	if brake > 0.01:
		if vx > 0.6:
			brake_force = -brake * float(spec["brake_force"])
		elif vx > -0.6 and throttle < 0.05:
			brake_force = -brake * float(spec["reverse_force"])
		else:
			brake_force = brake * float(spec["brake_force"]) * clampf(absf(vx) / 2.0, 0.0, 1.0)
	if handbrake:
		if absf(vx) > 0.5:
			brake_force -= signf(vx) * 5200.0
		else:
			brake_force = 0.0

	var drag_scale := 2.2 if not on_track else 1.0
	var drag_k := 0.5 * RHO * float(spec["drag_area"]) * drag_scale
	var fx_drag := -drag_k * speed * vx
	var fy_drag := -drag_k * speed * vy

	var rolling := 0.0
	if absf(vx) > 0.15:
		rolling = -signf(vx) * m * g * 0.013 * (3.4 if not on_track else 1.0)

	# Círculo de fricción: lo que se usa para acelerar/frenar no está
	# disponible para agarrar en curva.
	var rear_use := clampf(absf(traction + brake_force * 0.45) / maxf(grip_force_r, 1.0), 0.0, 0.92)
	var lat_cap_r := grip_force_r * sqrt(maxf(0.35, 1.0 - rear_use * rear_use))
	if handbrake:
		lat_cap_r *= 0.36
	var front_use := clampf(absf(brake_force * 0.55) / maxf(grip_force_f, 1.0), 0.0, 1.0)
	var lat_cap_f := grip_force_f * sqrt(maxf(0.0, 1.0 - front_use * front_use))

	# Ángulos de deslizamiento. El guardia de 1.8 m/s evita la singularidad
	# cuando el coche está parado.
	var vx_guard := maxf(absf(vx), 1.8)
	var alpha_f := atan2(vy + lf * yaw_rate, vx_guard) - steer
	var alpha_r := atan2(vy - lr * yaw_rate, vx_guard)

	var fyf := clampf(-stiffness_f * alpha_f, -lat_cap_f, lat_cap_f)
	var fyr := clampf(-stiffness_r * alpha_r, -lat_cap_r, lat_cap_r)

	# Ecuaciones del modelo de bicicleta (x adelante, y derecha).
	var fx_total := traction + brake_force + fx_drag + rolling - fyf * sin(steer)
	var fy_total := fyf * cos(steer) + fyr + fy_drag
	var ax := fx_total / m + vy * yaw_rate
	var ay := fy_total / m - vx * yaw_rate
	var yaw_acc := (lf * fyf * cos(steer) - lr * fyr) / iz

	vx += ax * dt
	vy += ay * dt
	yaw_rate += yaw_acc * dt

	# A baja velocidad el modelo se vuelve inestable: se mezcla con la
	# cinemática pura para que el coche pueda maniobrar y aparcar.
	var low := clampf(1.0 - speed / 3.2, 0.0, 1.0)
	if low > 0.0:
		var kinematic := vx * tan(steer) / wb
		yaw_rate = lerpf(yaw_rate, kinematic, low * 0.95)
		vy *= 1.0 - low * 0.85

	yaw_rate = clampf(yaw_rate, -3.2, 3.2)
	velocity = fwd * vx + right * vy

	speed_kmh = velocity.length() * 3.6
	slip = clampf((absf(alpha_r) - 0.05) * 8.0 + wheelspin * 0.35, 0.0, 1.0)
	lateral_g = ay / g
	longitudinal_g = ax / g
	dbg = {"af": alpha_f, "ar": alpha_r, "fyf": fyf, "fyr": fyr, "capr": lat_cap_r, "yacc": yaw_acc, "vx": vx, "vy": vy}
	var demand_abs := absf(demand)
	wheelspin = clampf((demand_abs / maxf(traction_cap, 1.0) - 0.92) * 2.2, 0.0, 1.0)


func _update_effects(delta: float) -> void:
	brake_light = move_toward(brake_light, 1.0 if (brake > 0.05 or handbrake) else 0.0, delta * 8.0)
	var smoking := slip > 0.28 or (handbrake and speed_kmh > 8.0)
	for particles in _smoke:
		particles.emitting = smoking
		particles.modulate.a = clampf(slip * 1.5, 0.25, 1.0)


func _make_smoke() -> CPUParticles2D:
	if _smoke_texture == null:
		_smoke_texture = _make_soft_circle()
	var p := CPUParticles2D.new()
	p.amount = 16
	p.lifetime = 0.8
	p.local_coords = false
	p.emission_shape = CPUParticles2D.EMISSION_SHAPE_SPHERE
	p.emission_sphere_radius = 0.35
	p.direction = Vector2(0, -1)
	p.spread = 180.0
	p.initial_velocity_min = 0.8
	p.initial_velocity_max = 2.8
	p.gravity = Vector2(0, -3.5)
	p.damping_min = 1.0
	p.damping_max = 2.5
	p.scale_amount_min = 0.4
	p.scale_amount_max = 1.2
	p.color = Color(0.86, 0.87, 0.88, 0.30)
	p.texture = _smoke_texture
	p.z_index = 8
	p.emitting = false
	return p


static func _make_soft_circle() -> ImageTexture:
	var size := 32
	var image := Image.create_empty(size, size, false, Image.FORMAT_RGBA8)
	for y in size:
		for x in size:
			var d := Vector2(x - (size - 1) * 0.5, y - (size - 1) * 0.5).length() / (size * 0.5)
			var a := clampf(1.0 - d, 0.0, 1.0)
			image.set_pixel(x, y, Color(1, 1, 1, a * a))
	return ImageTexture.create_from_image(image)


func _draw() -> void:
	if spec.is_empty():
		return
	CarArt.draw_car(self, spec, steer, brake_light)
