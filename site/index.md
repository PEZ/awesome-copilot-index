---
layout: default
title: Awesome Copilot Index
---

# Awesome Copilot Index

This site hosts an daily generated index of the [awesome-copilot](https://github.com/github/awesome-copilot) repository, providing structured access to GitHub Copilot instructions, prompts, and chat modes.

<div id="stats" style="background: #f6f8fa; border-radius: 6px; padding: 15px; margin: 20px 0;">
<p>Loading statistics...</p>
</div>


## Download Index Files

- [JSON Format](index.json)
- [EDN Format](index.edn)

## Index Structure

Each entry in the index contains:

- `filename` - Original markdown filename
- `title` - Extracted from H1 heading or generated from filename
- `description` - From frontmatter if available
- `link` - Relative path to the file in the repository

## Usage Examples

You can use these index files to:

- Build search interfaces for Copilot resources
- Create filtered views by category (instructions, prompts, chatmodes)
- Generate documentation or catalogs
- Power recommendation systems

---

Source for generating: [pez/awesome-copilot-index](https://github.com/pez/awesome-copilot-index)

<script>
// Load and display statistics from the JSON index
fetch('index.json')
    .then(response => response.json())
    .then(data => {
        const statsDiv = document.getElementById('stats');
        const instructionCount = data.instructions?.length || 0;
        const promptCount = data.prompts?.length || 0;
        const chatmodeCount = data.chatmodes?.length || 0;
        const totalCount = instructionCount + promptCount + chatmodeCount;

        statsDiv.innerHTML = `
            <p><strong>Total Resources:</strong> ${totalCount}</p>
            <ul>
                <li><strong>Instructions:</strong> ${instructionCount}</li>
                <li><strong>Prompts:</strong> ${promptCount}</li>
                <li><strong>Chat Modes:</strong> ${chatmodeCount}</li>
            </ul>
            <p><small>Last updated: ${new Date(data.generated).toLocaleString()}</small></p>
        `;
    })
    .catch(error => {
        document.getElementById('stats').innerHTML = '<p>Statistics unavailable</p>';
        console.error('Error loading index:', error);
    });
</script>
