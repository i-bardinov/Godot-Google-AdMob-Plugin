extends Node

class_name AdMob, "res://scripts/admob-lib/icon.png"

# signals
signal initialization_complete

signal banner_ad_loaded
signal banner_ad_failed_to_load(error_code)
signal banner_ad_opened
signal banner_ad_clicked
signal banner_ad_left_application
signal banner_ad_closed

signal interstitial_ad_loaded
signal interstitial_ad_failed_to_load(error)
signal interstitial_ad_opened
signal interstitial_ad_clicked
signal interstitial_ad_left_application
signal interstitial_ad_closed

signal rewarded_ad_loaded
signal rewarded_ad_failed_to_load(error)
signal rewarded_ad_opened
signal rewarded_ad_closed
signal rewarded_ad_earned_reward(type, amount)
signal rewarded_ad_failed_to_show(error)

# properties
export var is_real: bool setget is_real_set
export var banner_on_top: bool = true
export var banner_id: String
export var interstitial_id: String
export var rewarded_id: String
export var child_directed: bool = false
export var is_personalized: bool = true
export(String, "G", "PG", "T", "MA") var max_ad_content_rate

# "private" properties
var _admob_singleton = null
var _interstitial_ad_loaded = false
var _rewarded_ad_loaded = false

func _enter_tree():
	if not init():
		print("AdMob Java Singleton not found")

# setters
func is_real_set(new_val) -> void:
	is_real = new_val
# warning-ignore:return_value_discarded
	init()
	
func child_directed_set(new_val) -> void:
	child_directed = new_val
# warning-ignore:return_value_discarded
	init()

func is_personalized_set(new_val) -> void:
	is_personalized = new_val
# warning-ignore:return_value_discarded
	init()

func max_ad_content_rate_set(new_val) -> void:
	if new_val != "G" and new_val != "PG" \
		and new_val != "T" and new_val != "MA":
			
		max_ad_content_rate = "G"
		print("Invalid max_ad_content_rate, using 'G'")


# initialization
func init() -> bool:
	if(Engine.has_singleton("GodotAdMob")):
		_admob_singleton = Engine.get_singleton("GodotAdMob")

		_admob_singleton.connect("on_initialization_complete", self, "_on_initialization_complete");
		
		_admob_singleton.connect("on_banner_ad_loaded", self, "_on_banner_ad_loaded");
		_admob_singleton.connect("on_banner_ad_failed_to_load", self, "_on_banner_ad_failed_to_load");
		_admob_singleton.connect("on_banner_ad_opened", self, "_on_banner_ad_opened");
		_admob_singleton.connect("on_banner_ad_clicked", self, "_on_banner_ad_clicked");
		_admob_singleton.connect("on_banner_ad_left_application", self, "_on_banner_ad_left_application");
		_admob_singleton.connect("on_banner_ad_closed", self, "_on_banner_ad_closed");
		
		_admob_singleton.connect("on_interstitial_ad_loaded", self, "_on_interstitial_ad_loaded");
		_admob_singleton.connect("on_interstitial_ad_failed_to_load", self, "_on_interstitial_ad_failed_to_load");
		_admob_singleton.connect("on_interstitial_ad_opened", self, "_on_interstitial_ad_opened");
		_admob_singleton.connect("on_interstitial_ad_clicked", self, "_on_interstitial_ad_clicked");
		_admob_singleton.connect("on_interstitial_ad_left_application", self, "_on_interstitial_ad_left_application");
		_admob_singleton.connect("on_interstitial_ad_closed", self, "_on_interstitial_ad_closed");
		
		_admob_singleton.connect("on_rewarded_ad_loaded", self, "_on_rewarded_ad_loaded");
		_admob_singleton.connect("on_rewarded_ad_failed_to_load", self, "_on_rewarded_ad_failed_to_load");
		_admob_singleton.connect("on_rewarded_ad_opened", self, "_on_rewarded_ad_opened");
		_admob_singleton.connect("on_rewarded_ad_closed", self, "_on_rewarded_ad_closed");
		_admob_singleton.connect("on_rewarded_ad_earned_reward", self, "_on_rewarded_ad_earned_reward");
		_admob_singleton.connect("on_rewarded_ad_failed_to_show", self, "_on_rewarded_ad_failed_to_show");

		_admob_singleton.initWithContentRating(
			is_real,
			child_directed,
			is_personalized,
			max_ad_content_rate
		)
		return true
	return false

func is_available() -> bool:
	return _admob_singleton != null
# load

func load_banner() -> void:
	if _admob_singleton != null:
		_admob_singleton.loadBanner(banner_id, banner_on_top)

func load_interstitial() -> void:
	if _admob_singleton != null:
		_admob_singleton.loadInterstitial(interstitial_id)
		
func is_interstitial_loaded() -> bool:
	return _interstitial_ad_loaded
		
func load_rewarded_ad() -> void:
	if _admob_singleton != null:
		_admob_singleton.loadRewardedAd(rewarded_id)
		
func is_rewarded_ad_loaded() -> bool:
	return _rewarded_ad_loaded

# show / hide

func show_banner() -> void:
	if _admob_singleton != null:
		_admob_singleton.showBanner()
		
func hide_banner() -> void:
	if _admob_singleton != null:
		_admob_singleton.hideBanner()

func move_banner(on_top: bool) -> void:
	if _admob_singleton != null:
		banner_on_top = on_top
		_admob_singleton.move(banner_on_top)

func show_interstitial() -> void:
	if _admob_singleton != null:
		_admob_singleton.showInterstitial(interstitial_id)
		
func show_rewarded_ad() -> void:
	if _admob_singleton != null:
		_admob_singleton.showRewardedAd(rewarded_id)

# resize

func banner_resize() -> void:
	if _admob_singleton != null:
		_admob_singleton.resize()
		
# dimension
func get_banner_dimension() -> Vector2:
	if _admob_singleton != null:
		return Vector2(_admob_singleton.getBannerWidth(), _admob_singleton.getBannerHeight())
	return Vector2()

# callbacks

func _on_initialization_complete() -> void:
	emit_signal("initialization_complete")

func _on_banner_ad_loaded() -> void:
	emit_signal("banner_ad_loaded")
	
func _on_banner_ad_failed_to_load(error: String) -> void:
	emit_signal("banner_ad_failed_to_load", error)

func _on_banner_ad_opened() -> void:
	emit_signal("banner_ad_opened")

func _on_banner_ad_clicked() -> void:
	emit_signal("banner_ad_clicked")

func _on_banner_ad_left_application() -> void:
	emit_signal("banner_ad_left_application")

func _on_banner_ad_closed() -> void:
	emit_signal("banner_ad_closed")

func _on_interstitial_ad_loaded() -> void:
	_interstitial_ad_loaded = true
	emit_signal("interstitial_ad_loaded")

func _on_interstitial_ad_failed_to_load(error: String) -> void:
	_interstitial_ad_loaded = false
	emit_signal("interstitial_ad_failed_to_load", error)

func _on_interstitial_ad_opened() -> void:
	_interstitial_ad_loaded = false
	emit_signal("interstitial_ad_opened")

func _on_interstitial_ad_clicked() -> void:
	emit_signal("interstitial_ad_clicked")

func _on_interstitial_ad_left_application() -> void:
	emit_signal("interstitial_ad_left_application")

func _on_interstitial_ad_closed() -> void:
	_interstitial_ad_loaded = false
	emit_signal("interstitial_ad_closed")

func _on_rewarded_ad_loaded() -> void:
	_rewarded_ad_loaded = true
	emit_signal("rewarded_ad_loaded")

func _on_rewarded_ad_failed_to_load(error: String) -> void:
	emit_signal("rewarded_ad_failed_to_load", error)
	
func _on_rewarded_ad_opened() -> void:
	_rewarded_ad_loaded = false
	emit_signal("rewarded_ad_opened")
	
func _on_rewarded_ad_closed() -> void:
	_rewarded_ad_loaded = false
	emit_signal("rewarded_ad_closed")
	
func _on_rewarded_ad_earned_reward(type: String, amount: int) -> void:
	emit_signal("rewarded_ad_earned_reward", type, amount)
	
func _on_rewarded_ad_failed_to_show(error: String) -> void:
	emit_signal("rewarded_ad_failed_to_show", error)

