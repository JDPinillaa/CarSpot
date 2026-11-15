class_name Powertrain
extends RefCounted

## Cadena cinemática: motor de combustión + caja de cambios automática.
##
## Es lógica pura (sin nodos ni entrada). Recibe la velocidad angular de las
## ruedas motrices y el acelerador, y devuelve la fuerza de tracción en
## newtons. Los parámetros salen de CarSpecs, así que cada coche tiene su
## propia curva de par, relaciones y régimen de corte.

const IDLE_RPM := 900.0
const LIMITER_RPM := 7500.0
const WHEEL_RADIUS := 0.33

var idle_rpm := IDLE_RPM
var limiter_rpm := LIMITER_RPM
var upshift_rpm := 6400.0
var downshift_rpm := 2100.0
var shift_time := 0.32
var shift_time_down := 0.18
var final_drive := 4.1
var efficiency := 0.9
var engine_brake_factor := 0.08
var launch_rpm := 2600.0
var gear_ratios: Array = [3.6, 2.3, 1.7, 1.3, 1.0]
var torque_curve: Array = [
	Vector2(800, 130), Vector2(2000, 240), Vector2(3500, 310),
	Vector2(4500, 330), Vector2(5500, 315), Vector2(6500, 275),
	Vector2(7500, 215),
]

var gear := 1
var rpm := IDLE_RPM
var shift_timer := 0.0
var limiter_active := false
var auto_shift := true
var wheelspin := 0.0


func configure(spec: Dictionary) -> void:
	gear_ratios = spec["gear_ratios"].duplicate()
	torque_curve = spec["torque_curve"].duplicate()
	final_drive = float(spec["final_drive"])
	upshift_rpm = float(spec["shift_up"])
	downshift_rpm = float(spec["shift_down"])
	limiter_rpm = float(spec["limiter"])
	launch_rpm = float(spec["launch_rpm"])
	reset()


## Avanza la simulación un paso y devuelve la fuerza de tracción (N).
## Un valor negativo representa freno de motor.
func update(delta: float, wheel_angular_speed: float, throttle: float) -> float:
	wheelspin = 0.0
	var ratio: float = float(gear_ratios[gear - 1]) * final_drive
	var wheel_rpm: float = absf(wheel_angular_speed) * ratio * 60.0 / TAU
	# El embrague patina a baja velocidad: el motor sube de vueltas antes de
	# que el coche empiece a moverse, que es lo que se oye y se ve en el tacómetro.
	var launch_target: float = idle_rpm + throttle * launch_rpm
	var target_rpm: float = maxf(wheel_rpm, launch_target)
	if wheel_rpm > 200.0:
		target_rpm = maxf(wheel_rpm, idle_rpm + throttle * 400.0)
	rpm = lerpf(rpm, target_rpm, clampf(delta * 10.0, 0.0, 1.0))

	if shift_timer > 0.0:
		shift_timer -= delta
		return 0.0

	if auto_shift:
		if wheel_rpm > upshift_rpm and gear < gear_ratios.size() and throttle > 0.05:
			gear += 1
			shift_timer = shift_time
			return 0.0
		if wheel_rpm < downshift_rpm and gear > 1:
			gear -= 1
			shift_timer = shift_time_down
			return 0.0

	limiter_active = rpm >= limiter_rpm and throttle > 0.05
	if limiter_active:
		return 0.0

	var torque := _torque_at(rpm) * throttle
	if throttle < 0.05 and absf(wheel_angular_speed) > 0.5:
		torque = -_torque_at(rpm) * engine_brake_factor

	return torque * ratio * efficiency / WHEEL_RADIUS


func _torque_at(revs: float) -> float:
	if torque_curve.is_empty():
		return 0.0
	if revs <= torque_curve[0].x:
		return torque_curve[0].y
	for i in range(1, torque_curve.size()):
		var a: Vector2 = torque_curve[i - 1]
		var b: Vector2 = torque_curve[i]
		if revs <= b.x:
			return lerpf(a.y, b.y, (revs - a.x) / maxf(0.001, b.x - a.x))
	return torque_curve[torque_curve.size() - 1].y


func reset() -> void:
	gear = 1
	rpm = idle_rpm
	shift_timer = 0.0
	limiter_active = false
	wheelspin = 0.0
