extends Node2D

@export var battery_life := 100

func turn_on():
    if has_node("Light2D"):
        $Light2D.visible = true

func turn_off():
    if has_node("Light2D"):
        $Light2D.visible = false

func _process(delta):
    if has_node("Light2D") and $Light2D.visible:
        battery_life = max(0, battery_life - delta * 2)
