extends Node2D

@export var chase_speed := 140
var chasing := false
var target := null

func _process(delta):
    if chasing and target:
        var dir = target.global_position - global_position
        if dir.length() > 5:
            global_position += dir.normalized() * chase_speed * delta

func start_chase(t):
    target = t
    chasing = true

func stop_chase():
    chasing = false
    target = null
