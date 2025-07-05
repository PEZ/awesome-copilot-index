---
layout: default
title: Awesome Copilot Index
description: Daily generated index of the awesome-copilot repository, providing structured access to GitHub Copilot instructions, prompts, and chat modes.
---

# Awesome Copilot Index

This site hosts a daily generated index of the [awesome-copilot](https://github.com/github/awesome-copilot) repository, providing structured access to its GitHub Copilot instructions, prompts, and chat modes.


| Category | Count |
|----------|------:|
| **Instructions** | {{instruction-count}} |
| **Prompts** | {{prompt-count}} |
| **Chat Modes** | {{chatmode-count}} |
| **Total Resources** | {{total-count}} |

*Last updated: {{formatted-date}}*

The index powers the **Awesome Copilot** menu in VS Code together with [Joyride](https://github.com/BetterThanTomorrow/joyride) and this script:
- [Awesome Copilot Joyride Script](awesome-copilot-script)
The script includes instructions for how to install it in VS Code/Joyride.

![Awesome Copilot menu](awesome-copilot-menu.png)

## Download Index Files

- [JSON Format](awesome-copilot.json)
- [EDN Format](awesome-copilot.edn)

## Index Structure

Each entry in the index contains:

- `filename` - Original markdown filename
- `title` - Extracted from H1 heading or generated from filename
- `description` - From frontmatter if available
- `link` - Relative path to the file in the repository

---

<div class="sponsors-section" markdown="1">

## Support This Project ♥️

If you find this project useful, consider sponsoring me:

[![GitHub Sponsors](https://img.shields.io/github/sponsors/pez?style=for-the-badge&logo=github&logoColor=white&labelColor=black&color=ff69b4)](https://github.com/sponsors/pez)

Thanks for considering!

</div>

---

Source for this site: [pez/awesome-copilot-index](https://github.com/pez/awesome-copilot-index)
