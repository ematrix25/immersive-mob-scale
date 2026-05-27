# Immersive Mob Scale

Immersive Mob Scale is a configurable Fabric mod that scales Minecraft entities based on configurable category systems. Instead of hardcoded mob sizes, entities are grouped into categories with customizable scale and movement values through JSON configuration files.

---

## Features

- Category-based mob scaling with JSON configuration system
- Lightweight and highly configurable

---

## Philosophy

Minecraft entities should feel more proportionally believable. Tiny creatures should actually feel tiny, while larger animals should maintain more natural proportions.

The mod prioritizes:
- Configurability over hardcoding
- Compatibility over invasive changes
- Immersion through subtle scaling

---

## Current Categories

Default configuration includes categories such as:

- Tiny insects
- Small insects
- Large insects
- Amphibians
- Small animals
- Aquatic animals
- Medium animals

Each category supports:
- Scale modifier
- Speed modifier
- Entity lists

---

## Configuration

Configuration files are automatically generated at:

```txt
/config/immersivemobscale/
```

Current config files: 

```txt
categories.json
```
Example:

```json
{
  "small_animals": {
    "scale": 0.68,
    "speed": 1.09,
    "entities": [
      "minecraft:rabbit",
      "minecraft:fox"
    ]
  }
}
```

---

## Setup

Requirements:

- Minecraft Fabric
- Fabric API
- Java 25

Development

Clone repository:

```bash
git clone https://github.com/ematrix25/immersive-mob-scale.git
```

Run Gradle:

```bash
./gradlew runClient
```

---

## Development Status

Current version:

```txt
v0.1.0
```

Implemented systems:
- Generic config loading
- Entity category system

Planned features:
- Runtime entity scaling
- Renderer integration
- Hitbox scaling

---

## License

This project is licensed under the MIT License.