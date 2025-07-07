---
layout: default
title: Awesome Cursor Rules to Copilot Joyride Script
description: Browse and convert Awesome Cursor Rules to Copilot Instructions, Prompts Chat modes right from inside VS Code with this Joyride Script
---

# Awesome Cursor Rules to Copilot Joyride Script

Browse and convert [Awesome Cursor Rules](https://github.com/PatrickJS/awesome-cursorrules) to Copilot [Instructions](https://code.visualstudio.com/docs/copilot/copilot-customization#_custom-instructions), [Prompts](https://code.visualstudio.com/docs/copilot/copilot-customization#_prompt-files-experimental), and [Chat modes](https://code.visualstudio.com/docs/copilot/chat/chat-modes) right from inside VS Code with this [Joyride](https://github.com/BetterThanTomorrow/joyride) Script.

As the [Awesome Copilot repository](https://github.com/github/awesome-copilot) grows you may not always find the configuration you need using the [Awesome Copilot VS Code Menu](awesome-copilot-script.md). That's where this script comes in, because maybe the content you need is available at Awesome Cursor Rules.


<!-- {% include youtubePlayer.html id=page.youtubeId %} -->

## Installation Instructions

1. **Install Joyride** from the VS Code Extensions marketplace
2. **Copy the script below**
3. **In VS Code Command Palette**: `Joyride: Create User Script...`
4. **Name it**: `awesome-cursorrules-to-copilot`
5. **Paste the script** in the editor that opens

## Usage

1. **Command Palette**: `Joyride: Run User Script...`
2. **Select**: `awesome_cursorrules_to_copilot.cljs`

The script presents a sort of “wizard”, with a series of quick-pick menus. The menu for the actual Awesome Cursor Rules content is fuzzy-searchable so you can quickly filter it for the content you are looking for.

The menu presents Cursor Rules Components files from [Awesome Cursor Rules](https://github.com/PatrickJS/awesome-cursorrules). At the source cursor rules are most often organized into “tech stack” with several components. Each component corresponds roughly to a problem domain. The menu will present this as a flat list of components and will convert to files named from both the tech stack and the problem domain.

This does not always translate cleanly to either Copilot [Instructions](https://code.visualstudio.com/docs/copilot/copilot-customization#_custom-instructions), [Prompts](https://code.visualstudio.com/docs/copilot/copilot-customization#_prompt-files-experimental), or [Chat modes](https://code.visualstudio.com/docs/copilot/chat/chat-modes), but you'll choose one of these per component and that's a start.

The menus remember their last choice, so you can view the contents or the README of an item first, and then quickly find the same item to install it.

When you have confirmed that you have Copilot configuration that is helpful, consider contributing them to the [Awesome Copilot repository](https://github.com/github/awesome-copilot).

See also: [Awesome Copilot VS Code Menu](awesome-copilot-script.md)

---

## Script Source

<textarea class="code" readonly>
{% include_relative awesome_cursorrules_to_copilot.cljs %}
</textarea>

---

[← Back to Index](index.html) | [Download Script](awesome_cursorrules_to_copilot.cljs)
![alt text](image.png)