#!/usr/bin/env python3
"""Build the Adventure UI fork on a local filesystem, including upstream checks."""

import filecmp
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile


def main():
    repository = Path(__file__).resolve().parents[1]
    identifier = hashlib.sha256(str(repository).encode()).hexdigest()[:12]
    staging = Path(tempfile.gettempdir()) / ("forge-workshop-build-" + identifier)
    staging.mkdir(parents=True, exist_ok=True)
    development = repository / ".dev"
    development.mkdir(exist_ok=True)

    # External FAT volumes create AppleDouble files beside Java source and class
    # files. Mirror actual tracked inputs onto the local disk before Maven scans.
    tracked = subprocess.check_output(
        ["git", "ls-files", "-z"], cwd=repository
    ).decode().split("\0")
    inputs = set()
    for name in tracked:
        if not name or name.startswith("forge-gui/res/"):
            continue
        source = repository / name
        if not source.is_file():
            continue
        inputs.add(name)
        destination = staging / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        if not destination.exists() or not filecmp.cmp(source, destination, shallow=False):
            shutil.copyfile(source, destination)

    manifest = staging / ".workshop-inputs"
    if manifest.exists():
        for obsolete in set(manifest.read_text().splitlines()) - inputs:
            path = staging / obsolete
            if path.is_file():
                path.unlink()
    manifest.write_text("\n".join(sorted(inputs)))

    resources = staging / "forge-gui" / "res"
    if not resources.exists():
        resources.symlink_to(repository / "forge-gui" / "res", target_is_directory=True)

    java_home = os.environ.get("FORGE_JAVA_HOME", os.environ.get("JAVA_HOME"))
    if not java_home:
        java_home = "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
    maven = os.environ.get("FORGE_MAVEN") or shutil.which("mvn")
    if not maven:
        maven = str(repository.parent / ".forge-tools/apache-maven-3.9.16/bin/mvn")
    if not Path(java_home, "bin", "java").exists() or not Path(maven).exists():
        raise SystemExit("Set FORGE_JAVA_HOME to a JDK 17 installation and FORGE_MAVEN to Maven.")
    environment = dict(os.environ, JAVA_HOME=java_home)
    dependency_cache = repository.parent / ".forge-tools" / "m2"
    print("Building Forge Workshop in", staging, flush=True)
    subprocess.run([
        maven, "-B", "-ntp", "-Dmaven.repo.local=" + str(dependency_cache),
        "-pl", "forge-gui-mobile-dev", "-am", "package"
    ], cwd=staging, env=environment, check=True)

    jars = list((staging / "forge-gui-mobile-dev/target").glob("*-jar-with-dependencies.jar"))
    if len(jars) != 1:
        raise SystemExit("Expected one packaged Adventure application.")
    distribution = development / "dist"
    distribution.mkdir(exist_ok=True)
    shutil.copyfile(jars[0], distribution / "forge-workshop.jar")
    (development / "build-path.txt").write_text(str(staging) + "\n")
    subprocess.run(["python3", str(repository / "scripts/workshop-profile.py")], check=True)
    print("Built", distribution / "forge-workshop.jar", flush=True)


if __name__ == "__main__":
    main()
