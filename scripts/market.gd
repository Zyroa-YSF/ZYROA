extends Node2D

var door_locked := true
var vendor_alerted := false

func _ready():
    $StorageArea.connect("body_entered", Callable(self, "_on_storage_entered"))

func _on_storage_entered(body):
    if body.name == "Player":
        # Player can try to jump out of the storage window - require repeated jump presses
        body.set_meta("in_storage", true)

# Called by player when interacting in storage
func attempt_escape(player):
    var required_presses := 6
    var presses := 0
    player.set_meta("escape_presses", 0)
    # Wait loop - in practice, player script should increment a counter; here we implement a simple blocking loop for prototype
    while presses < required_presses:
        if player.get_meta("escape_presses") != presses:
            presses = player.get_meta("escape_presses")
        yield(get_tree().create_timer(0.1), "timeout")
        # safety break
        if presses > 20:
            break
    if presses >= required_presses:
        get_tree().change_scene_to_file("res://scenes/street.tscn")
    else:
        # failed, stay
        pass

func cashier_seen():
    vendor_alerted = true
    # cashier's eyes white logic placeholder
