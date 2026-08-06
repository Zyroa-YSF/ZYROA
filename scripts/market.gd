extends Node2D

var door_locked := true
var vendor_alerted := false

func _ready():
    if has_node("StorageArea"):
        $StorageArea.connect("body_entered", Callable(self, "_on_storage_entered"))

func _on_storage_entered(body):
    if body.name == "Player":
        # Player can try to jump out of the storage window - require repeated jump presses
        body.set_meta("in_storage", true)

# Called by player when interacting in storage
func attempt_escape(player):
    var required_presses := 6
    var presses := 0
    if player.has_meta("escape_presses"):
        presses = player.get_meta("escape_presses")

    var safety_counter := 0
    # Poll until presses reached or safety timeout
    while presses < required_presses and safety_counter < 200:
        await get_tree().create_timer(0.1).timeout
        if player.has_meta("escape_presses"):
            presses = player.get_meta("escape_presses")
        safety_counter += 1
    if presses >= required_presses:
        get_tree().change_scene_to_file("res://scenes/street.tscn")
    else:
        # failed or timed out, stay
        pass

func cashier_seen():
    vendor_alerted = true
    # cashier's eyes white logic placeholder
