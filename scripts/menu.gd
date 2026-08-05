extends Control

func _ready():
    $VBox/StartButton.connect("pressed", Callable(self, "_on_start_pressed"))
    $VBox/QuitButton.connect("pressed", Callable(self, "_on_quit_pressed"))

func _on_start_pressed():
    # Start the game via GameManager
    if Engine.has_singleton("GameManager"):
        var gm = Engine.get_singleton("GameManager")
        gm.start_game()
    else:
        get_tree().change_scene_to_file("res://scenes/main.tscn")

func _on_quit_pressed():
    get_tree().quit()
