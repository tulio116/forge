#!/usr/bin/env python3
"""Configure a development profile without opening the player's normal saves."""

import os
from pathlib import Path


def main():
    repository = Path(__file__).resolve().parents[1]
    development = repository / ".dev"
    profile = development / "profile"
    cache = development / "cache"
    shared_pictures = Path(os.environ.get("FORGE_CARD_PICTURES", "/Volumes/Seagate/Forge/card-pictures"))
    if not shared_pictures.is_dir():
        shared_pictures = cache / "pics" / "cards"
    for path in [profile / "preferences", cache, shared_pictures]:
        path.mkdir(parents=True, exist_ok=True)

    configuration = repository / "forge-gui" / "forge.profile.properties"
    user_setting = "userDir=" + str(profile)
    if configuration.exists():
        if user_setting not in configuration.read_text().splitlines():
            raise SystemExit("Existing Forge profile differs from the isolated Workshop profile; review it before launching.")
    else:
        configuration.write_text(
            "# Local Workshop profile; keep this file out of Git.\n" + user_setting
            + "\ncacheDir=" + str(cache) + "\ncardPicsDir=" + str(shared_pictures) + "\n"
        )
    print("Workshop saves:", profile)
    print("Card pictures:", shared_pictures)


if __name__ == "__main__":
    main()
