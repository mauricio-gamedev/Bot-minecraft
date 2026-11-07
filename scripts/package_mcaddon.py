#!/usr/bin/env python3
"""Package the Behavior Pack and Resource Pack into a .mcaddon file."""
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "dist/MC-Companion-Bedrock-0.1.0.mcaddon"
OUT.parent.mkdir(parents=True, exist_ok=True)
PACKS = [
    (ROOT / "packs/behavior_pack", "MC-Companion_BP"),
    (ROOT / "packs/resource_pack", "MC-Companion_RP"),
]
with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED) as archive:
    for source, prefix in PACKS:
        for path in source.rglob("*"):
            if path.is_file():
                archive.write(path, (Path(prefix) / path.relative_to(source)).as_posix())
print(OUT)
