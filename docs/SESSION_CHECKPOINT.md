# Session checkpoint — 2026-11-07

## Goal
An original Bedrock companion that follows the player, roams nearby, defends its owner, collects dropped items, and returns collected items. An Android sidecar provides an OpenRouter chat overlay.

## Current implementation
- Behavior Pack entity identifier: mccompanion:buddy.
- Resource Pack includes custom humanoid geometry, 64x64 pixel texture, and movement animations.
- Entity can be tamed with an apple, bread, cookie, or golden carrot.
- Native AI groups separate combat, passive defense, following, wandering, and item collection.
- Native owner interaction exposes a touchscreen action label; the Script API opens an owner-only ActionFormData menu for follow, explore, guard, stay, and collect orders.
- Guard actively targets hostile mobs nearby. Stay does not proactively seek hostiles but retaliates if the owner or entity is attacked.
- Item collection is limited to dropped items in the world and handoff to the owner.
- Android helper is a separate app with an overlay chat, direct OpenRouter request, locally encrypted API key, and locally encrypted last-16-message chat history.

## Known limitations
- No direct bridge from the chat model into entity AI. The app does not inspect the world or control entity movement.
- No autonomous mining, block harvesting, crafting, full player-like pathfinding, or world-state awareness.
- A Bedrock entity runs only while its world/chunk is loaded; the Android app cannot keep Minecraft's world simulation alive after the game unloads or closes.
- The 0.2.0 touchscreen order menu and the updated AI mode transitions have passed JSON/schema-shape and script-syntax checks, but must still be runtime-tested inside Minecraft on the target phone.
- The API interaction is intentionally limited to empty-hand owner interaction so food and item use are not hijacked by the menu.

## Validation performed
- GitHub Actions validates pack JSON, Behavior Pack script module dependencies, entity order groups, owner/empty-hand interaction structure, geometry and texture references, PNG integrity, and JavaScript syntax.
- GitHub Actions successfully packages the 0.2.0 MCAddon.
- The Android debug APK build succeeds in GitHub Actions. App overlay, OpenRouter authentication, persistent memory, and real-device behavior should continue to be tested on the target phone.