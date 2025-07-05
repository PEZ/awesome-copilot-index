---
layout: default
title: Awesome Copilot Joyride Script
description: The script that powers the Awesome Copilot menu in VS Code via Joyride, providing easy access to GitHub Copilot instructions, prompts, and chat modes.
youtubeId: AiL8LurZgSI
---

# Awesome Copilot Joyride Script

This script powers the Awesome Copilot menu in VS Code via [Joyride](https://github.com/BetterThanTomorrow/joyride).

{% include youtubePlayer.html id=page.youtubeId %}

## Installation Instructions

1. **Install Joyride** from the VS Code Extensions marketplace
2. **Copy the script below**
3. **In VS Code Command Palette**: `Joyride: Create User Script...`
4. **Name it**: `awesome-copilot`
5. **Paste the script** in the editor that opens

## Usage

- **Command Palette**: `Joyride: Run User Script...`
- **Select**: `awesome_copilot.cljs`

---

## Script Source

<textarea class="code" readonly>
{% include_relative awesome_copilot.cljs %}
</textarea>

---

[← Back to Index](index.html) | [Download Script](awesome_copilot.cljs)
