---
layout: default
title: Awesome Copilot Joyride Script
description: The script that powers the Awesome Copilot menu in VS Code via Joyride, providing easy access to GitHub Copilot instructions, prompts, and chat modes.
youtubeId: AiL8LurZgSI
---

<div class="page-navigation top">
  <a href="index.html">← Back to Index</a>
  <span class="separator">|</span>
  <a href="awesome_copilot.cljs">Download Script</a>
  <span class="separator">|</span>
  <a href="https://github.com/PEZ/awesome-copilot-index" target="_blank" rel="noopener" class="github-badge">
    <img src="https://img.shields.io/badge/fork%20me%20on-GitHub-24292f?style=for-the-badge&logo=github&logoColor=white" alt="Fork me on GitHub" />
  </a>
</div>

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

The script presents a sort of “wizard”, with a series of quick-pick menus. The menu for the actual Awesome Copilot content is fuzzy-searchable so you can quickly filter it for the content you are looking for.

The menus remember their last choice, so you can view the contents of an item first, and then quickly find the same item to install it.

See also: [Cursor Rules to Copilot Joyride Script](cursorrules-to-copilot-script)

---

## Script Source

<textarea class="code" readonly>
{% include_relative awesome_copilot.cljs %}
</textarea>

---

<div class="page-navigation bottom">
  <a href="index.html">← Back to Index</a>
  <span class="separator">|</span>
  <a href="awesome_copilot.cljs">Download Script</a>
  <span class="separator">|</span>
  <a href="https://github.com/PEZ/awesome-copilot-index" target="_blank" rel="noopener" class="github-badge">
    <img src="https://img.shields.io/badge/fork%20me%20on-GitHub-24292f?style=for-the-badge&logo=github&logoColor=white" alt="Fork me on GitHub" />
  </a>
</div>
