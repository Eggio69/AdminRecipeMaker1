# AdminRecipeMaker — Fabric 1.21.11

Server-side mod prototype for operator-managed shaped crafting recipes with exact `ItemStack` component matching. It is designed so regular players do not need the mod installed on their clients: the editor uses Minecraft's vanilla generic chest screen.

## Build

**Important:** Do not build this version with Gradle 8.14. Loom 1.14.10 uses the newer Gradle plugin API, and older Loom versions can fail while preparing newer Fabric API modules (for example, `fabric-content-registries-v0`).

- JDK 21
- Gradle 9.2.1 or newer (Minecraft 1.21.11 requires a recent Loom; this project uses Loom 1.14.10)
- Fabric Loom downloads Minecraft/Yarn/Fabric dependencies on first build

Check that `gradle --version` reports Gradle 9.2.1 or newer and that Gradle runs on JDK 21. Then run `gradle build` from the `AdminRecipeMaker` project directory. The jar will be in `build/libs/`. If you still see cached Loom or Minecraft setup errors, stop Gradle and run `gradle --stop`, then retry with `gradle build --refresh-dependencies`.

## Install

Put the built `adminrecipemaker-0.1.0.jar` into the server's `mods` directory. Fabric API is required. This is a server mod; no client installation is intended.

## Commands (OP level 2)

- `/recipe create <id>` — opens a vanilla 9×6 chest editor.
- Place the 3×3 recipe ingredients in chest slots 1–9.
- Place the output stack in chest slot 14 (row 2, column 5).
- `/recipe save <id>` — saves the recipe and its exact output stack.
- `/recipe list` — list recipe IDs.
- `/recipe delete <id>` — delete a recipe.
- `/recipe reload` — reload recipes from disk.

Recipes are stored in `<world>/config/adminrecipemaker/recipes.json`. The stack codec stores item IDs and data components, including custom names, lore, enchantments and custom data where supported by Minecraft's item-stack codec.

## Matching rules

This prototype uses a fixed 3×3 shaped grid. Empty cells must remain empty, ingredient item IDs and components must match exactly, and each occupied cell must contain at least the configured ingredient count. Mirrored/rotated patterns and shapeless recipes are not implemented yet.

## Important prototype notes

- Test on a backup world first; this is an initial source prototype and has not been compiled against your exact server environment in this workspace.
- This version replaces the crafting result when a matching custom recipe is found and consumes its ingredients when the result is taken. Bulk shift-click crafting and recipe remainder items (for example buckets) need additional handling before treating this as production-ready.
- If a custom recipe matches, it takes precedence over the vanilla result for that grid.


## Build with Gradle 9.2.1

The Loom 1.14.10 plugin requires Gradle 9.2 or newer. If your installed `gradle` command reports 8.14, do not run `gradle build` directly. On Windows, run `build-with-gradle-9.2.1.bat`; on Linux/macOS, run `bash build-with-gradle-9.2.1.sh`. These scripts download Gradle 9.2.1 into a project-local `.gradle-tools` directory and use it for the build. JDK 21 must be installed and `JAVA_HOME` must point to it. The script is a build helper; it does not guarantee the mod source compiles.
