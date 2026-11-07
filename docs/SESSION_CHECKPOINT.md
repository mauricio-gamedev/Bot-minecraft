# Session checkpoint — 2026-11-07

## Goal
An original Bedrock companion that can follow the player, roam nearby, defend its owner, collect dropped items, and return collected items. An Android helper app provides an OpenRouter chat overlay.

## Current implementation
- Behavior Pack entity identifier: `mccompanion:buddy`.
- Resource Pack includes a custom humanoid geometry and a 64x64 pixel texture.
- Entity can be tamed with an apple, bread, cookie, or golden carrot.
- Tamed behavior uses Bedrock AI goals for owner defense, melee combat, dropped-item pickup, item handoff, following, and nearby wandering.
- Android helper is a separate app; its foreground service supports a floating chat window. Chat history is persisted locally with AES-GCM using Android Keystore.
- The Behavior Pack now includes stable Script API dependencies and an in-game owner menu for follow, explore, guard, stay, and collect orders.

## Known limitations
- No direct bridge from the chat model into the entity AI. The app does not inspect the world or control entity movement.
- Pickup is for dropped items. Autonomous mining, block harvesting, crafting, and full player-like navigation are not implemented in this foundation.
- A Bedrock entity runs only while its world/chunk is loaded; the Android app cannot keep Minecraft's world simulation alive after the game unloads or closes.
- The new 0.2.0 script-based order menu has not yet been runtime-tested inside Minecraft on the target phone. Re-test taming, owner-only menu access, each order, combat, and item pickup.

## Validation performed
- JSON syntax, manifest UUID linkage, geometry identifier, texture reference, and PNG integrity are checked by `scripts/validate_packs.py`.
- Runtime compatibility, pathfinding quality, tame interaction, inventory pickup, combat target filters, and Android overlay behavior still require device testing.
