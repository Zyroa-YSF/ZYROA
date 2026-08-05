extends Node2D

@onready var player := $PlayerInstance
@onready var level := $Level

func _ready():
    # Place player at a default spawn point inside the level
    player.global_position = Vector2(240, 220)
    # Wire basic interaction signals
    player.connect("interacted", Callable(self, "_on_player_interact"))
    player.connect("attacked", Callable(self, "_on_player_attack"))

func _on_player_interact():
    # Forward interact to nearby level elements - placeholder
    var overlapping = player.get_overlapping_areas() if player.has_method("get_overlapping_areas") else []
    # In practice, player script sets metadata when near interactable areas; here we rely on area callbacks
    pass

func _on_player_attack():
    # placeholder for attack handling
    pass
