#!/usr/bin/env python3
"""Create a native Mac launcher with a bundled runtime and isolated saves."""

import os
from pathlib import Path
import shlex
import shutil
import subprocess
import tempfile


def main():
    repository = Path(__file__).resolve().parents[1]
    development = repository / ".dev"
    jar = development / "dist/forge-workshop.jar"
    if not jar.exists():
        raise SystemExit("Run python3 scripts/build-workshop.py first.")
    subprocess.run(["python3", str(repository / "scripts/workshop-profile.py")], check=True)
    java_home = Path(os.environ.get("FORGE_JAVA_HOME",
                     "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"))
    build = Path((development / "build-path.txt").read_text().strip())
    launch_script = build / "forge-gui-mobile-dev/target/forge-adventure.sh"
    java_line = next(line for line in launch_script.read_text().splitlines() if line.startswith("java "))
    arguments = shlex.split(java_line)
    options = []
    iterator = iter(arguments[1:arguments.index("-jar")])
    for option in iterator:
        options.append(option + "=" + next(iterator) if option == "--add-opens" else option)

    applications = Path(os.environ.get("FORGE_WORKSHOP_APPS", str(development / "apps"))).expanduser()
    destination = applications / "Forge Workshop.app"
    if destination.exists():
        raise SystemExit("Workshop app already exists; close and move it aside before repackaging.")
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="forge-workshop-package-") as temporary:
        temporary = Path(temporary)
        inputs = temporary / "input"
        inputs.mkdir()
        shutil.copyfile(jar, inputs / "forge-workshop.jar")
        command = [str(java_home / "bin/jpackage"), "--type", "app-image",
                   "--name", "Forge Workshop", "--app-version", "1.0.0",
                   "--mac-package-identifier", "org.tulio.forge.workshop",
                   "--input", str(inputs), "--main-jar", "forge-workshop.jar",
                   "--main-class", "forge.app.Main", "--runtime-image", str(java_home),
                   "--dest", str(temporary / "output")]
        for option in options + ["-Dforge.assetsDir=" + str(repository / "forge-gui") + "/"]:
            command += ["--java-options", option]
        subprocess.run(command, check=True)
        shutil.copytree(temporary / "output/Forge Workshop.app", destination)
    print("Mac application:", destination)


if __name__ == "__main__":
    main()
