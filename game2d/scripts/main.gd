extends Node2D

## Punto de entrada: menú, construcción del circuito, cámara dinámica,
## cronometraje por sectores, fantasma de la mejor vuelta, marcas de derrape,
## audio y pruebas automáticas.
##
## Modos de prueba (argumentos tras `--`):
##   --smoke           20 s de piloto automático y sale
##   --laps            150 s de piloto automático y sale
##   --shot            captura una carrera en /tmp/simulador2d_shot.png
##   --menu_shot       captura el menú en /tmp/simulador2d_menu.png
##   --car=N --track=N elige coche y circuito para las pruebas

enum State { MENU, COUNTDOWN, RACE, PAUSED }

const COUNTDOWN_TIME := 3.2
const SECTORS := 3
const GHOST_INTERVAL := 0.05
const SHOT_PATH := "/tmp/simulador2d_shot.png"
const MENU_SHOT_PATH := "/tmp/simulador2d_menu.png"

var state := State.MENU

var menu: Menu
var menu_layer: CanvasLayer
var hud: Hud
var hud_layer: CanvasLayer
var world: Node2D
var track: RaceTrack
var car: RaceCar
var ghost: GhostCar
var skids: SkidMarks
var camera: Camera2D
var engine_audio: EngineAudio

var car_index := 0
var track_index := 0

var countdown := 0.0
var go_flash := 0.0
var lap_time := 0.0
var last_lap := -1.0
var best_lap := -1.0
var lap_count := 0

var _lap_progress := 0.0
var _prev_progress := 0.0
var _last_index := 0
var _lap_valid := true
var _sector_index := 0
var _sectors: Array = []
var _sector_deltas: Array = []
var _best_sectors: Array = []

var _ghost_record: Array = []
var _ghost_best: Array = []
var _ghost_timer := 0.0
var _ghost_cursor := 0

var _skid_last: Array = []
var _message := ""
var _message_time := 0.0

var _cam_zoom := 10.4
var _heading_up := true

# Pruebas y capturas.
var _autopilot_mode := false
var _smoke_mode := false
var _laps_mode := false
var _shot_mode := false
var _menu_shot := false
var _shot_time := 0.0
var _shot_done := false
var _test_time := 0.0
var _test_last_second := -1
var _stuck_time := 0.0
var _offtrack_time := 0.0
var _max_speed := 0.0
var _test_duration := -1.0


func _ready() -> void:
	_setup_input()
	_setup_camera()
	_setup_hud()
	_setup_menu()
	_parse_args()

	if _menu_shot:
		menu.active = true
		set_process(true)
		return

	if _smoke_mode or _laps_mode or _shot_mode:
		_autopilot_mode = true
		_start_race(car_index, track_index)
		menu.visible = false
		menu.active = false
		countdown = 0.35


func _process(delta: float) -> void:
	if _menu_shot:
		_shot_time += delta
		if _shot_time > 0.7 and not _shot_done:
			_shot_done = true
			_capture(MENU_SHOT_PATH)
		return

	if state == State.MENU:
		return

	if state == State.COUNTDOWN:
		countdown -= delta
		if countdown <= 0.0:
			_begin_race()

	if state == State.RACE:
		_update_camera(delta)
		_update_ghost()
		go_flash = maxf(0.0, go_flash - delta)

	if state == State.RACE or state == State.COUNTDOWN:
		_message_time = maxf(0.0, _message_time - delta)
		_update_audio()

	_update_hud()

	if _shot_mode:
		_shot_time += delta
		if _shot_time > 5.5 and not _shot_done:
			_shot_done = true
			_capture(SHOT_PATH)

	if _smoke_mode or _laps_mode:
		_run_test(delta)


func _physics_process(delta: float) -> void:
	if state != State.RACE:
		return
	if _autopilot_mode:
		_autopilot(delta)
	_race_step(delta)
	_update_skids()
	if not car.on_track and car.speed_kmh > 5.0:
		_offtrack_time += delta


# --- Configuración ----------------------------------------------------------

func _setup_input() -> void:
	_add_action("throttle", [KEY_W, KEY_UP])
	_add_action("brake", [KEY_S, KEY_DOWN])
	_add_action("steer_left", [KEY_A, KEY_LEFT])
	_add_action("steer_right", [KEY_D, KEY_RIGHT])
	_add_action("handbrake", [KEY_SPACE])
	_add_action("reset", [KEY_R])
	_add_action("camera", [KEY_C])
	_add_action("ghost_toggle", [KEY_G])
	_add_action("raceline", [KEY_L])
	_add_action("pause", [KEY_ESCAPE, KEY_P])
	_add_action("menu_prev", [KEY_A, KEY_LEFT])
	_add_action("menu_next", [KEY_D, KEY_RIGHT])
	_add_action("menu_up", [KEY_W, KEY_UP])
	_add_action("menu_down", [KEY_S, KEY_DOWN])
	_add_action("start", [KEY_ENTER, KEY_KP_ENTER])


func _add_action(action: String, keys: Array) -> void:
	if not InputMap.has_action(action):
		InputMap.add_action(action)
	for code in keys:
		var event := InputEventKey.new()
		event.physical_keycode = code
		if not InputMap.action_has_event(action, event):
			InputMap.action_add_event(action, event)


func _setup_camera() -> void:
	camera = Camera2D.new()
	camera.enabled = true
	camera.position = Vector2.ZERO
	add_child(camera)
	camera.make_current()


func _setup_hud() -> void:
	hud_layer = CanvasLayer.new()
	hud_layer.layer = 2
	add_child(hud_layer)
	hud = Hud.new()
	hud_layer.add_child(hud)
	hud.visible = false


func _setup_menu() -> void:
	# En un CanvasLayer para que la cámara del mundo no desplace la interfaz.
	menu_layer = CanvasLayer.new()
	menu_layer.layer = 3
	add_child(menu_layer)
	menu = Menu.new()
	menu_layer.add_child(menu)
	menu.start_requested.connect(_on_start_requested)


func _parse_args() -> void:
	var args := OS.get_cmdline_user_args()
	_smoke_mode = "--smoke" in args
	_laps_mode = "--laps" in args
	_shot_mode = "--shot" in args
	_menu_shot = "--menu_shot" in args
	for arg in args:
		if arg.begins_with("--car="):
			car_index = clampi(int(arg.substr(6)), 0, CarSpecs.count() - 1)
		elif arg.begins_with("--track="):
			track_index = clampi(int(arg.substr(8)), 0, TrackData.count() - 1)
		elif arg.begins_with("--time="):
			_test_duration = maxf(5.0, float(arg.substr(7)))


# --- Flujo de juego ---------------------------------------------------------

func _on_start_requested(selected_car: int, selected_track: int) -> void:
	_start_race(selected_car, selected_track)


func _start_race(new_car: int, new_track: int) -> void:
	car_index = clampi(new_car, 0, CarSpecs.count() - 1)
	track_index = clampi(new_track, 0, TrackData.count() - 1)

	_clear_world()

	state = State.COUNTDOWN
	countdown = COUNTDOWN_TIME
	go_flash = 0.0
	lap_time = 0.0
	last_lap = -1.0
	best_lap = -1.0
	lap_count = 0
	_lap_progress = 0.0
	_prev_progress = 0.0
	_lap_valid = true
	_sector_index = 0
	_sectors.clear()
	_sector_deltas.clear()
	_best_sectors.clear()
	_ghost_record.clear()
	_ghost_best.clear()
	_ghost_cursor = 0
	_ghost_timer = 0.0
	_message = ""
	_message_time = 0.0
	_offtrack_time = 0.0
	_max_speed = 0.0

	var spec := CarSpecs.get_car(car_index)
	var data := TrackData.get_track(track_index)

	world = Node2D.new()
	world.name = "Mundo"
	add_child(world)

	track = RaceTrack.new()
	track.name = "Pista"
	world.add_child(track)
	track.build(data)

	skids = SkidMarks.new()
	skids.name = "Marcas"
	world.add_child(skids)

	ghost = GhostCar.new()
	ghost.name = "Fantasma"
	world.add_child(ghost)
	ghost.setup(spec)

	car = RaceCar.new()
	car.name = "Coche"
	world.add_child(car)
	car.setup(spec, track)
	car.active = false
	car.input_enabled = not _autopilot_mode

	_skid_last = car.rear_wheel_positions()
	_last_index = car.track_index
	_prev_progress = float(_last_index) / float(track.points.size())

	camera.position = car.global_position
	camera.rotation = car.heading + PI * 0.5
	_cam_zoom = 10.4
	camera.zoom = Vector2(_cam_zoom, _cam_zoom)

	hud.set_track(track)
	hud.visible = true

	_menu_flags(false)
	_setup_audio()

	print("Carrera: %s en %s (%.0f m)" % [spec["name"], track.display_name, track.total_length])


func _begin_race() -> void:
	state = State.RACE
	countdown = 0.0
	go_flash = 1.3
	lap_time = 0.0
	_lap_progress = 0.0
	_sector_index = 0
	_sectors.clear()
	_sector_deltas.clear()
	_ghost_record.clear()
	_ghost_cursor = 0
	_ghost_timer = 0.0
	_lap_valid = true
	car.active = true
	car.input_enabled = not _autopilot_mode
	_last_index = car.track_index
	_prev_progress = float(_last_index) / float(track.points.size())
	_skid_last = car.rear_wheel_positions()


func _menu_flags(show_menu: bool) -> void:
	menu.visible = show_menu
	menu.active = show_menu
	hud.visible = not show_menu


func _to_menu() -> void:
	_clear_world()
	state = State.MENU
	_message = ""
	_message_time = 0.0
	_menu_flags(true)
	if engine_audio != null:
		engine_audio.stop_engine()


func _clear_world() -> void:
	if world != null:
		world.queue_free()
	world = null
	car = null
	track = null
	ghost = null
	skids = null


func _setup_audio() -> void:
	if DisplayServer.get_name() == "headless":
		return
	if engine_audio == null:
		engine_audio = EngineAudio.new()
		add_child(engine_audio)
	engine_audio.start_engine()
	engine_audio.set_state(Powertrain.IDLE_RPM, 0.0, 0.0, 0.0)


# --- Cronometraje -----------------------------------------------------------

func _race_step(delta: float) -> void:
	lap_time += delta
	_ghost_timer += delta

	var idx := track.nearest_index(car.global_position, _last_index)
	_last_index = idx
	var progress := float(idx) / float(track.points.size())
	var dp := wrapf(progress - _prev_progress, -0.5, 0.5)
	if absf(dp) > 0.12:
		dp = 0.0  # salto por reinicio o teleport
	_prev_progress = progress
	_lap_progress += dp * track.total_length

	# El progreso se acumula en metros. Al cruzar la meta el último salto es
	# un wrap (de ~1 a ~0) que `wrapf` convierte en un salto pequeño pero
	# positivo; por eso una vuelta se cierra al superar la longitud total.
	if _lap_progress >= track.total_length:
		_lap_progress -= track.total_length
		if _lap_valid and _sector_index >= SECTORS - 1:
			_complete_lap()
		else:
			_begin_timed_lap()

	if _lap_valid and _sector_index < SECTORS - 1:
		var boundary := track.total_length * float(_sector_index + 1) / float(SECTORS)
		if _lap_progress >= boundary:
			_sector_index += 1
			_sectors.append(lap_time)
			_record_sector_delta()
			_sector_flash_message()

	if _lap_valid and _ghost_timer >= GHOST_INTERVAL:
		_ghost_timer = 0.0
		_ghost_record.append({
			"t": lap_time,
			"p": car.global_position,
			"h": car.heading,
		})

	_max_speed = maxf(_max_speed, car.speed_kmh)


func _record_sector_delta() -> void:
	_sector_deltas.clear()
	for i in _sector_index:
		var current_start := 0.0 if i == 0 else float(_sectors[i - 1])
		var current := float(_sectors[i]) - current_start
		if i < _best_sectors.size():
			var best_start := 0.0 if i == 0 else float(_best_sectors[i - 1])
			var best := float(_best_sectors[i]) - best_start
			_sector_deltas.append(current - best)
		else:
			_sector_deltas.append(0.0)


func _sector_flash_message() -> void:
	var i := _sector_index - 1
	var delta := float(_sector_deltas[i]) if i < _sector_deltas.size() else 0.0
	if absf(delta) < 0.005:
		_show_message("Sector %d" % (i + 1), 1.6)
	elif delta < 0.0:
		_show_message("Sector %d  %.2f" % [i + 1, delta], 2.0)
	else:
		_show_message("Sector %d  +%.2f" % [i + 1, delta], 2.0)


func _complete_lap() -> void:
	var lap := lap_time
	last_lap = lap
	lap_count += 1

	if best_lap < 0.0 or lap < best_lap:
		best_lap = lap
		_best_sectors = _sectors.duplicate()
		_ghost_best = _ghost_record.duplicate()
		_show_message("¡Mejor vuelta!  %s" % _format_time(lap), 3.2)
		print("Mejor vuelta: %.3f s" % lap)
	else:
		_show_message("Vuelta %d  ·  %s" % [lap_count, _format_time(lap)], 2.2)

	lap_time = 0.0
	_sector_index = 0
	_sectors.clear()
	_sector_deltas.clear()
	_ghost_record.clear()
	_ghost_cursor = 0
	_ghost_timer = 0.0
	_lap_valid = true


func _begin_timed_lap() -> void:
	lap_time = 0.0
	_sector_index = 0
	_sectors.clear()
	_sector_deltas.clear()
	_ghost_record.clear()
	_ghost_cursor = 0
	_ghost_timer = 0.0
	_lap_valid = true
	_show_message("Cronómetro activo", 1.8)


func _reset_car() -> void:
	if car == null or state != State.RACE:
		return
	var idx := track.nearest_index(car.global_position)
	car.reset_at(track.points[idx], track.tangents[idx].angle())
	_lap_valid = false
	# Mantener la distancia real recorrida: si vuelves a la pista a mitad de
	# vuelta, la meta y los sectores siguen donde les corresponde.
	_lap_progress = track.distance_at(idx)
	_prev_progress = float(idx) / float(track.points.size())
	_last_index = idx
	_sector_index = 0
	_sectors.clear()
	_sector_deltas.clear()
	_ghost_record.clear()
	_ghost_cursor = 0
	_ghost_timer = 0.0
	_show_message("Vuelve a la meta para cronometrar", 3.0)
	_skid_last = car.rear_wheel_positions()


# --- Cámara, HUD, audio, marcas --------------------------------------------

func _update_camera(delta: float) -> void:
	if car == null:
		return
	var speed_frac := clampf(car.speed_kmh / 250.0, 0.0, 1.0)
	var target_zoom := lerpf(10.4, 6.4, speed_frac)
	_cam_zoom = lerpf(_cam_zoom, target_zoom, clampf(delta * 2.2, 0.0, 1.0))
	camera.zoom = Vector2(_cam_zoom, _cam_zoom)
	camera.position = camera.position.lerp(
		car.global_position + car.velocity * 0.16, clampf(delta * 7.0, 0.0, 1.0))
	var target_rotation := (car.heading + PI * 0.5) if _heading_up else 0.0
	camera.rotation = lerp_angle(camera.rotation, target_rotation, clampf(delta * 5.0, 0.0, 1.0))

	if not car.on_track and car.speed_kmh > 20.0:
		var amp := 1.4 * (6.0 / maxf(1.0, _cam_zoom))
		camera.offset = Vector2(randf_range(-amp, amp), randf_range(-amp, amp))
	else:
		camera.offset = camera.offset.lerp(Vector2.ZERO, clampf(delta * 8.0, 0.0, 1.0))


func _update_ghost() -> void:
	if ghost == null:
		return
	if _ghost_best.size() < 2 or lap_time <= 0.0:
		ghost.visible = false
		return

	var target := lap_time
	while _ghost_cursor + 1 < _ghost_best.size() and float(_ghost_best[_ghost_cursor + 1]["t"]) <= target:
		_ghost_cursor += 1

	if _ghost_cursor >= _ghost_best.size() - 1:
		var last_time := float(_ghost_best[_ghost_best.size() - 1]["t"])
		ghost.visible = target <= last_time + 0.15
		if ghost.visible:
			ghost.global_position = _ghost_best[_ghost_best.size() - 1]["p"]
			ghost.rotation = _ghost_best[_ghost_best.size() - 1]["h"]
		return

	var a: Dictionary = _ghost_best[_ghost_cursor]
	var b: Dictionary = _ghost_best[_ghost_cursor + 1]
	var span := maxf(0.001, float(b["t"]) - float(a["t"]))
	var f := clampf((target - float(a["t"])) / span, 0.0, 1.0)
	ghost.visible = true
	var pa: Vector2 = a["p"]
	var pb: Vector2 = b["p"]
	ghost.global_position = pa.lerp(pb, f)
	ghost.rotation = lerp_angle(float(a["h"]), float(b["h"]), f)


func _update_hud() -> void:
	if hud == null or car == null:
		return
	var tel := car.telemetry()
	hud.state = {
		"car_name": car.spec["name"],
		"car_color": car.spec["color"],
		"track_name": track.display_name,
		"speed_kmh": tel["speed_kmh"],
		"rpm": tel["rpm"],
		"rpm_max": float(car.spec["limiter"]),
		"gear": tel["gear"],
		"throttle": car.throttle,
		"brake": car.brake,
		"lap_time": lap_time,
		"best_lap": best_lap,
		"last_lap": last_lap,
		"laps": lap_count,
		"sectors": _sectors,
		"sector_deltas": _sector_deltas,
		"progress": _lap_progress / maxf(1.0, track.total_length),
		"lap_valid": _lap_valid,
		"countdown": countdown if state == State.COUNTDOWN else 0.0,
		"go_flash": go_flash,
		"paused": state == State.PAUSED,
		"message": _message,
		"message_time": _message_time,
		"on_track": car.on_track,
		"car_pos": car.global_position,
		"car_heading": car.heading,
		"ghost_pos": ghost.global_position if (ghost != null and ghost.visible) else null,
		"max_speed": 300.0,
		"lateral_g": tel["lateral_g"],
		"shift": tel["shift"],
	}
	hud.queue_redraw()


func _update_audio() -> void:
	if engine_audio == null or car == null:
		return
	var tel := car.telemetry()
	var throttle := car.throttle if state == State.RACE else 0.0
	engine_audio.set_state(tel["rpm"], throttle, tel["slip"], tel["speed_kmh"])


func _update_skids() -> void:
	if skids == null or car == null:
		return
	var sliding := car.slip > 0.30 or (car.handbrake and car.speed_kmh > 10.0)
	if not sliding:
		_skid_last = car.rear_wheel_positions()
		return
	var wheels := car.rear_wheel_positions()
	for i in wheels.size():
		if _skid_last.size() <= i:
			_skid_last.append(wheels[i])
		var previous: Vector2 = _skid_last[i]
		if previous.distance_to(wheels[i]) > 0.30:
			skids.add_segment(previous, wheels[i], 0.35 + car.slip * 0.65)
			_skid_last[i] = wheels[i]


func _show_message(text: String, time: float) -> void:
	_message = text
	_message_time = time


# --- Entrada ----------------------------------------------------------------

func _unhandled_input(event: InputEvent) -> void:
	if state == State.MENU:
		return
	var key := event as InputEventKey
	if key == null or not key.pressed or key.echo:
		return
	match key.physical_keycode:
		KEY_ESCAPE, KEY_P:
			_toggle_pause()
		KEY_R:
			_reset_car()
		KEY_C:
			_heading_up = not _heading_up
		KEY_G:
			if ghost != null:
				ghost.visible = not ghost.visible
		KEY_L:
			if track != null:
				track.toggle_raceline()
		KEY_M:
			_to_menu()


func _toggle_pause() -> void:
	if state == State.RACE:
		state = State.PAUSED
		if car != null:
			car.active = false
		if engine_audio != null:
			engine_audio.stop_engine()
	elif state == State.PAUSED:
		state = State.RACE
		if car != null:
			car.active = true
		if engine_audio != null:
			engine_audio.start_engine()


# --- Piloto automático y pruebas -------------------------------------------

func _autopilot(delta: float) -> void:
	if car == null or track == null:
		return

	var speed := car.velocity.length()
	if car.speed_kmh < 4.0:
		_stuck_time += delta
	else:
		_stuck_time = 0.0
	if _stuck_time > 2.5:
		# Maniobra de desatasco: marcha atrás con el volante invertido.
		car.throttle = 0.0
		car.brake = 0.9
		car.steer_input = -car.steer_input
		if _stuck_time > 5.0:
			_stuck_time = 0.0
		return

	var idx := car.track_index
	var look := clampf(6.0 + speed * 0.42, 8.0, 30.0)
	var target := track.sample_at_distance(track.distance_at(idx) + look)
	var local := car.to_local(target)
	# Persecución pura: ángulo de volante que pasa por el punto objetivo.
	var dist_sq := maxf(local.length_squared(), 1.0)
	var delta_target := atan2(2.0 * float(car.spec["wheelbase"]) * local.y, dist_sq)
	var speed_factor := clampf(speed / 42.0, 0.0, 1.0)
	var max_steer := deg_to_rad(float(car.spec["max_steer_deg"])) * lerpf(1.0, 0.34, speed_factor)
	car.steer_input = clampf(delta_target / maxf(max_steer, 0.01), -1.0, 1.0)

	# Planificador de velocidad: para cada punto de los próximos 110 m se
	# calcula la velocidad máxima que aguanta la curva y cuánto se puede
	# llegar a ella frenando. El mínimo manda, así que frena antes de entrar.
	var v_target := _autopilot_speed_limit(idx)

	# En primera el par sobra: se dosifica el gas a baja velocidad.
	var err := v_target - speed
	var want_throttle := 0.0
	var want_brake := 0.0
	if err < -0.6:
		want_brake = clampf(-err / 2.6, 0.18, 1.0)
	elif err < 0.9:
		want_throttle = 0.30 if speed > 6.0 else 0.55
	else:
		want_throttle = clampf(0.58 + err * 0.06, 0.58, 1.0)

	# Con volante puesto no se acelera a fondo: el neumático trasero ya está
	# trabajando de lado y el gas lo hace perder agarre.
	if absf(car.steer_input) > 0.15:
		want_throttle = minf(want_throttle, lerpf(0.35, 0.15, clampf(absf(car.steer_input), 0.0, 1.0)))
	if car.slip > 0.30:
		want_throttle = 0.0

	car.throttle = move_toward(car.throttle, want_throttle, delta * 3.5)
	car.brake = move_toward(car.brake, want_brake, delta * 6.0)


## Velocidad (m/s) que el piloto puede llevar ahora sin salirse en los
## próximos 110 m. Combina el límite de agarre en curva con la frenada
## disponible, de modo que la velocidad cae a tiempo.
func _autopilot_speed_limit(idx: int) -> float:
	var n := track.points.size()
	var mu := 9.81 * float(car.spec["grip_rear"]) * 0.52
	var brake_accel := 6.0
	var limit := 45.0
	for k in range(0, 56, 2):
		var i := (idx + k) % n
		var curvature := absf(track.curvatures[i])
		var corner_speed := sqrt(mu / maxf(curvature, 0.00045))
		var distance := float(k) * RaceTrack.SAMPLE_SPACING
		var allowed := sqrt(corner_speed * corner_speed + 2.0 * brake_accel * distance)
		limit = minf(limit, allowed)
	return limit


func _run_test(delta: float) -> void:
	_test_time += delta
	var second := int(_test_time)
	if second != _test_last_second and car != null:
		_test_last_second = second
		var lateral := track.lateral_at(car.global_position, car.track_index)
		var d: Dictionary = car.dbg
		print("t=%3ds | %6.1f km/h | lat=%5.1f | pos=(%.0f,%.0f) | giro=%.2f/%.2f | gas=%.2f/%.2f | yaw=%.2f | vx=%.1f vy=%.1f | af=%.3f ar=%.3f | fyf=%.0f fyr=%.0f cap=%.0f | ya=%.1f | %s" % [
			second, car.speed_kmh, lateral, car.global_position.x, car.global_position.y,
			car.steer, car.steer_input, car.throttle, car.brake, car.yaw_rate,
			float(d.get("vx", 0.0)), float(d.get("vy", 0.0)),
			float(d.get("af", 0.0)), float(d.get("ar", 0.0)),
			float(d.get("fyf", 0.0)), float(d.get("fyr", 0.0)), float(d.get("capr", 0.0)),
			float(d.get("yacc", 0.0)),
			"OK" if car.on_track else "FUERA",
		])

	var duration := _test_duration if _test_duration > 0.0 else (150.0 if _laps_mode else 20.0)
	if _test_time > duration:
		print("SMOKE OK | vueltas=%d | mejor=%s | vel_max=%.1f km/h | fuera=%.1fs | marcas=%d | ghost=%d" % [
			lap_count, _format_time(best_lap), _max_speed, _offtrack_time,
			skids.count() if skids != null else 0, _ghost_best.size(),
		])
		get_tree().quit()


func _capture(path: String) -> void:
	await RenderingServer.frame_post_draw
	var image := get_viewport().get_texture().get_image()
	image.save_png(path)
	print("Captura guardada en ", path)
	get_tree().quit()


# --- Utilidades -------------------------------------------------------------

func _format_time(time: float) -> String:
	if time < 0.0:
		return "--:--.---"
	var minutes := int(time / 60.0)
	var seconds := time - minutes * 60.0
	return "%d:%06.3f" % [minutes, seconds]
