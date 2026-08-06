extends CharacterBody2D

@export var speed := 220
@export var jump_velocity := -400
@export var touch_move_speed := 180

signal interacted
signal attacked

@onready var ui := null

func _ready():
    Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
    if has_node("/root/HUD"):
        ui = get_node("/root/HUD")

func _physics_process(delta):
    var dir := Vector2.ZERO
    # Keyboard
    dir.x = Input.get_action_strength("ui_right") - Input.get_action_strength("ui_left")

    # Touch / virtual joystick placeholder: if touch input, use ui input actions (to be implemented in mobile controls)
    if dir.length() > 0:
        dir = dir.normalized()
        velocity.x = dir.x * speed
    else:
        velocity.x = lerp(velocity.x, 0, 0.2)

    if not is_on_floor():
        velocity.y += ProjectSettings.get_setting("physics/2d/default_gravity") * delta

    if Input.is_action_just_pressed("ui_accept") and is_on_floor():
        velocity.y = jump_velocity

    # Use CharacterBody2D convenience method
    move_and_slide()

    # Interaction
    if Input.is_action_just_pressed("interact"):
        emit_signal("interacted")

    # Attack
    if Input.is_action_just_pressed("attack"):
        emit_signal("attacked")

func _on_flashlight_battery_changed(value):
    if ui:
        if ui.has_method("update_battery"):
            ui.update_battery(value)
