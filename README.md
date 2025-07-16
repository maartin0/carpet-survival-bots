# Carpet survival bots

This mod allows any player to spawn and control a single Carpet bot in survival mode, bringing a powerful automation tool to all players.

Designed for Fabric 1.21, contributions are welcome

## Installation

Download the [latest release](https://github.com/maartin0/carpet-survival-bots/releases/latest) and make sure you also have [carpet](https://github.com/gnembon/fabric-carpet/releases/latest) installed.

Also available on [Modrinth](https://modrinth.com/mod/carpet-survival-bots).

## Usage

All commands are prefixed with `/bot`. The bot will be named after you (e.g., `#YourName`).

### Spawning your bot

To spawn your bot at your current location and orientation, run:
```bash
/bot spawn
```

### Controlling your bot

Once spawned, you can control your bot with various sub-commands. Here are a few examples:

*   **Start continuous mining:**
    ```bash
    /bot attack continuous
    ```

*   **Move forward:**
    ```bash
    /bot move forward
    ```

*   **Look at specific coordinates:**
    ```bash
    /bot look at 100 64 -200
    ```

*   **Stop all actions:**
    ```bash
    /bot stop
    ```

The bot can perform most actions that you can as a player such as using items, jumping, sneaking, sprinting, and managing its inventory. Explore the sub-commands by typing `/bot` and using the in-game command suggestions.