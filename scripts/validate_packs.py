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

if entity:
    description = entity.get("minecraft:entity", {}).get("description", {})
    if description.get("identifier") != "mccompanion:buddy":
        errors.append("Behavior entity identifier must be mccompanion:buddy")
    components = entity.get("minecraft:entity", {}).get("components", {})
    groups = entity.get("minecraft:entity", {}).get("component_groups", {})
    if "minecraft:behavior.random_stroll" not in components:
        errors.append("Entity is missing random_stroll exploration behavior")
    tamed = groups.get("mccompanion:tamed", {})
    for required in ("minecraft:behavior.follow_owner", "minecraft:behavior.melee_attack", "minecraft:behavior.pickup_items", "minecraft:behavior.go_and_give_items_to_owner"):
        if required not in tamed:
            errors.append(f"Tamed entity is missing {required}")

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
print("Pack JSON, manifest linkage, entity capabilities, geometry and textures: OK")
