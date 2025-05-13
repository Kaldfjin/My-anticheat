# MyAntiCheat

A simple and extendable Minecraft anti-cheat plugin for Paper 1.21.

## Features

- NoFall detection: Detects when players avoid fall damage illegitimately
- Speed detection: Identifies players moving faster than normally possible
- Fly detection: Monitors suspicious vertical movement and hovering
- Jesus (WaterWalk) detection: Catches players walking on water without legitimate means
- In-game notifications: Staff with proper permissions receive real-time cheat alerts
- Configurable checks and thresholds

## Compilation Using GitHub

To compile the plugin using GitHub Actions:

1. **Fork this repository** or push it to your own GitHub repository
2. **Go to the Actions tab** in your repository
3. **Select the "Build MyAntiCheat Plugin" workflow**
4. **Click on "Run workflow"** and select the branch you want to build from
5. Once the workflow completes, click on the completed workflow run
6. **Download the artifact** named "MyAntiCheat" which contains the compiled JAR file

Alternatively, you can simply push changes to your repository, and GitHub Actions will automatically build the plugin for you.

## Manual Compilation

If you prefer to build locally:

```bash
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Installation

1. Place the compiled JAR file in your server's `plugins` folder
2. Start or restart your Paper 1.21 server
3. Edit the configuration file at `plugins/MyAntiCheat/config.yml` as needed
4. Use `/myanticheat reload` to apply configuration changes

## Permissions

- `myanticheat.admin`: Access to admin commands like `/myanticheat reload`
- `myanticheat.notify`: Receive in-game notifications when players fail checks 