# Architecture

## 1. Bedrock Behavior Pack

The custom entity `mccompanion:buddy` owns all autonomous in-world behavior. It uses stable entity components rather than a remote model call for every action. A stable `@minecraft/server` + `@minecraft/server-ui` script opens an in-game order menu when the owner interacts with the tamed companion:

- Nearby idle roaming via `minecraft:behavior.random_stroll`.
- Owner-follow behavior after taming.
- Owner defense and hostile-mob targeting.
- Melee attack.
- Dropped-item pickup into the entity inventory and item handoff to its owner.
- Owner orders: follow, explore nearby, guard the current area, stay, and collect dropped items.

This keeps basic behavior independent from the phone app and from network availability.

## 2. Bedrock Resource Pack

Contains the original geometry, pixel texture, spawn-egg colors, and locomotion animations.

## 3. Android sidecar

The Android app provides a floating chat surface and sends HTTPS requests directly to OpenRouter using `openrouter/free`. It uses a foreground service for the app's overlay and Android Keystore to protect the locally entered API key.

The service does not keep Minecraft alive, read the game's world state, inspect the player's inventory, or command the custom entity. It cannot guarantee the Bedrock world simulation remains active after Minecraft is backgrounded or closed.

## 4. Future integration gate

Any later bridge from AI text to NPC actions must be explicit, opt-in, permission-limited, and tested on Bedrock Android. Do not use undocumented memory editing, arbitrary input injection, or claim world awareness without actual telemetry.
