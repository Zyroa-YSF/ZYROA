extends CharacterBody2D

@export var speed := 220
@export var jump_velocity := -400

func _ready():
    Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)

func _physics_process(delta):
    var dir := Vector2.ZERO
    dir.x = Input.get_action_strength("ui_right") - Input.get_action_strength("ui_left")
    if dir.length() > 0:
        dir = dir.normalized()
        velocity.x = dir.x * speed
    else:
        velocity.x = lerp(velocity.x, 0, 0.2)

    if not is_on_floor():
        velocity.y += ProjectSettings.get_setting("physics/2d/default_gravity") * delta

    if Input.is_action_just_pressed("ui_accept") and is_on_floor():
        velocity.y = jump_velocity

    move_and_slide()

func interact():
    # placeholder: interaction handler
    pass
