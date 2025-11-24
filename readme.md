# ByteChat

<p align="center">
  <a href="https://www.java.com/">
    <img src="https://img.shields.io/badge/Java-21+-blue" alt="Java"/>
  </a>
  <a href="https://papermc.io/">
    <img src="https://img.shields.io/badge/PaperMC-1.20.2%2B-green" alt="PaperMC"/>
  </a>
  <a href="license">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"/>
  </a>
  <a href="https://discord.com/invite/3K9yrZQRmS">
    <img src="https://img.shields.io/discord/1350369915521204276?label=Discord&color=7289DA&logo=discord&logoColor=white" alt="Discord"/>
  </a>
</p>

**ByteChat** is a modern and modular **chat formatting plugin** for **PaperMC**.  
It allows server owners to create elegant, dynamic, and interactive chat layouts using YAML configuration files.

---

## Overview

ByteChat replaces the default chat display with a flexible, component-based layout system that lets you fully control how messages look and behave.  
Each chat message is built from elements such as player names, prefixes, separators, and message bodies.  
Every element can include gradients, hover tooltips, and click actions — all defined directly through configuration.

---

<p align="center">
  <a href="https://discord.com/invite/3K9yrZQRmS">
    <img src="https://imgur.com/DvyC4jL.png" width="600" alt="ByteChat preview">
  </a>
  <br/>
  <i>If you need help, join the Discord server.</i>
</p>

---

## Features

- Supports **MiniMessage** (`<gradient:#fff:#000>`) and **LegacyAmpersand** (`&a`) serializers.
- Each chat format is fully customizable through YAML.
- Hover and click events for every element.
- PlaceholderAPI integration.
- Multiple format support with priority and permissions.
- Live reload command for configuration updates.
- Compatible with **PaperMC 1.20.2+**.

---
## Command Permissions

| Permission                 | Description                                       |
|----------------------------|---------------------------------------------------|
| `bytechat.command`         | Base permission for all ByteChat commands.        |
| `bytechat.command.reload`  | Allows the player to run `/bytechat reload`.      |
| `bytechat.command.mute`    | Allows the player to run `/bytechat mute`.        |

---

## Feature Permissions

| Permission             | Description                                      |
|------------------------|--------------------------------------------------|
| `bytechat.bypass.mute` | Allows the player to chat even if chat is muted. |

---

## Chat Format Permissions
| Permission                      | Description                              |
|---------------------------------|------------------------------------------|
| `bytechat.format.color`         | Allows color & formatting codes.         |
| `bytechat.format.mention`       | Allows mentioning players using `@name`. |
| `bytechat.format.tag.*`         | Allows use of all supported tags.        |
| `bytechat.format.tag.inventory` | Allows use of `[inv]` tag.               |
| `bytechat.format.tag.armor`     | Allows use of `[armor]` tag.             |
| `bytechat.format.tag.item`      | Allows use of `[item]` tag.              |


---

## Configuration Structure

The base configuration (`formats.yml`) defines how messages are displayed in chat.  
[Click here](paper/src/main/resources/formats.yml) to view the complete configuration structure.

---

## Installation

1. Download the plugin JAR file.
2. Place it inside your server’s `plugins` folder.
3. Start or reload your Paper server.
4. Edit the `config.yml` and `formats.yml` files to your liking.
5. Run `/bytechat reload` to apply changes.

---

## License

This project is released under the [MIT License](LICENSE), allowing free use, modification, and distribution.