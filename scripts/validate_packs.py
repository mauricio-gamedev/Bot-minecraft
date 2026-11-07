#!/usr/bin/env python3
"""Validate MC Companion Bedrock pack JSON and linked resources."""
from __future__ import annotations
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
BP = ROOT / "packs/behavior_pack"
RP = ROOT / "packs/resource_pack"
errors: list[str] = []

def read_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {exc}")
        return None

for path in (ROOT / "packs").rglob("*.json"):
    read_json(path)

bp_manifest = read_json(BP / "manifest.json")
rp_manifest = read_json(RP / "manifest.json")
entity = read_json(BP / "entities/companion.json")
client_entity = read_json(RP / "entity/companion.entity.json")
geometry = read_json(RP / "models/entity/companion.geo.json")

if bp_manifest and rp_manifest:
    dep = next((d for d in bp_manifest.get("dependencies", []) if d.get("uuid") == rp_manifest.get("header", {}).get("uuid")), None)
    if not dep:
        errors.append("Behavior Pack does not depend on the Resource Pack header UUID")
    if dep and dep.get("version") != rp_manifest.get("header", {}).get("version"):
        errors.append("Behavior Pack Resource Pack dependency version mismatch")

if bp_manifest:
    modules = bp_manifest.get("modules", [])
    script_module = next((m for m in modules if m.get("type") == "script"), None)
    if not script_module or script_module.get("entry") != "scripts/main.js":
        errors.append("Behavior Pack must include the scripts/main.js script module")
    else:
        script_path = BP / script_module["entry"]
        if not script_path.is_file():
            errors.append("Missing Behavior Pack script entry: scripts/main.js")
    dependencies = bp_manifest.get("dependencies", [])
    required_api = {
        "@minecraft/server": "2.11.0",
        "@minecraft/server-ui": "2.3.0",
    }
    for module_name, version in required_api.items():
        if not any(d.get("module_name") == module_name and d.get("version") == version for d in dependencies):
            errors.append(f"Missing stable script dependency {module_name} {version}")

if entity:
    description = entity.get("minecraft:entity", {}).get("description", {})
    if description.get("identifier") != "mccompanion:buddy":
        errors.append("Behavior entity identifier must be mccompanion:buddy")
    components = entity.get("minecraft:entity", {}).get("components", {})
    groups = entity.get("minecraft:entity", {}).get("component_groups", {})
    events = entity.get("minecraft:entity", {}).get("events", {})
    wild = groups.get("mccompanion:wild", {})
    core = groups.get("mccompanion:tamed_core", {})
    combat = groups.get("mccompanion:combat_mode", {})
    passive_defense = groups.get("mccompanion:passive_defense_mode", {})
    follow = groups.get("mccompanion:follow_mode", {})
    explore = groups.get("mccompanion:explore_mode", {})
    collect = groups.get("mccompanion:collect_mode", {})
    if "minecraft:behavior.random_stroll" not in wild or "minecraft:behavior.random_stroll" not in explore:
        errors.append("Wild and explore modes must provide random_stroll behavior")
    if "minecraft:is_tamed" not in core:
        errors.append("Tamed core is missing minecraft:is_tamed")
    for required in ("minecraft:behavior.owner_hurt_by_target", "minecraft:behavior.owner_hurt_target",
                     "minecraft:behavior.hurt_by_target", "minecraft:behavior.nearest_attackable_target",
                     "minecraft:behavior.melee_attack"):
        if required not in combat:
            errors.append(f"Combat mode is missing {required}")
    for required in ("minecraft:behavior.owner_hurt_by_target", "minecraft:behavior.owner_hurt_target",
                     "minecraft:behavior.hurt_by_target", "minecraft:behavior.melee_attack"):
        if required not in passive_defense:
            errors.append(f"Stay mode is missing passive-defense behavior {required}")
    if "minecraft:behavior.nearest_attackable_target" in passive_defense:
        errors.append("Stay mode should not actively seek nearby hostiles")
    if "minecraft:behavior.follow_owner" not in follow:
        errors.append("Follow mode is missing follow_owner behavior")
    for required in ("minecraft:behavior.pickup_items", "minecraft:behavior.go_and_give_items_to_owner"):
        if required not in collect:
            errors.append(f"Collect mode is missing {required}")
    for event_name in ("mccompanion:order_follow", "mccompanion:order_explore",
                       "mccompanion:order_guard", "mccompanion:order_stay", "mccompanion:order_collect"):
        if event_name not in events:
            errors.append(f"Missing companion order event {event_name}")

if client_entity and geometry:
    desc = client_entity.get("minecraft:client_entity", {}).get("description", {})
    geom_id = desc.get("geometry", {}).get("default")
    texture = desc.get("textures", {}).get("default")
    geometry_ids = {g.get("description", {}).get("identifier") for g in geometry.get("minecraft:geometry", [])}
    if geom_id not in geometry_ids:
        errors.append(f"Client entity geometry {geom_id!r} is not defined")
    if texture:
        texture_path = RP / (texture + ".png")
        if not texture_path.exists():
            errors.append(f"Missing entity texture: {texture_path.relative_to(ROOT)}")

try:
    from PIL import Image
    for png in (ROOT / "packs").rglob("*.png"):
        with Image.open(png) as image:
            image.verify()
except ImportError:
    print("Pillow not installed; skipped PNG integrity checks")
except Exception as exc:
    errors.append(f"PNG validation failed: {exc}")

if errors:
    for error in errors:
        print(f"ERROR: {error}")
    sys.exit(1)
print("Pack JSON, manifest linkage, script modules, companion orders, geometry and textures: OK")
