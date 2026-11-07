# Project instructions

- Preserve the split between Bedrock-native NPC behavior and the Android AI sidecar.
- Do not claim that the Android app reads Minecraft world state or controls entities unless a tested integration is added.
- Never commit API keys, secrets, signing keys, or local machine files.
- Keep `main` stable; implement work on feature branches and use pull requests.
- Validate all pack JSON and run `python scripts/validate_packs.py` after changing add-on assets.
- Keep the add-on compatible with stable Bedrock Android; avoid preview-only Script API dependencies unless explicitly justified.
- Preserve lightweight runtime behavior for low-memory Android devices.
- Record test evidence and known limitations in `docs/SESSION_CHECKPOINT.md`.
