# Copilot Instructions for Awesome Copilot Index

## Project Architecture

This project is a **multi-stage content indexing pipeline** that downloads, parses, and publishes GitHub repository content. The architecture consists of:

1. **Content Extraction**: Downloads GitHub repos as ZIP files (not git clones) to avoid history
2. **Markdown Parsing**: Parses frontmatter and content using custom trimming logic for edge cases
3. **Index Generation**: Transforms raw markdown into structured JSON/EDN for consumption
4. **Jekyll Publishing**: Renders a searchable web interface with responsive CSS
5. **VS Code Integration**: Provides ClojureScript-based Joyride script for in-editor access

**Key Data Flow**: `GitHub ZIP → Babashka Parser → JSON/EDN Index → Jekyll Site → Joyride Menu`

Always show what you evaluate in the repl to the user, using a codeblock that starts with the namespace in a `(in-ns ...)` form.

## Critical Workflows

### Main Build Pipeline
```bash
bb download-awesome        # Downloads awesome-copilot repo as ZIP
bb generate-index         # Parses markdown, generates site/awesome-copilot.{json,edn}
bb download-cursorrules   # Downloads awesome-cursorrules repo
bb generate-cursorrules-index  # Generates cursor rules index
```

### Development REPL Workflow
```clojure
(in-ns 'pez.index)
(require 'pez.index :reload)
;; Test parsing with problematic content
(def test-content "---\ntitle: test\n---  \n\n# Content")
(parse-markdown-file test-content)
```

### Jekyll Site Generation
- Templates use Selmer syntax: `{{instruction-count}}`, `{{formatted-date}}`
- Site builds from `site/` directory with generated JSON/EDN indices
- CSS supports both `ul` (bullet) and `ol` (numbered) lists with blue styling

### Development Template Workflow
When editing `site/index-template.md` during development:
1. **Edit the template file**: Make changes to `site/index-template.md`
2. **Regenerate index**: Run `bb generate-index` to process template with real data
3. **Jekyll auto-reload**: Jekyll's live reload automatically picks up the generated `index.md` changes

**Important**: Always edit the template file (`index-template.md`), not the generated file (`index.md`). The `bb generate-index` task uses existing Selmer template processing to generate `index.md` with real repository data, not placeholder values.

## Project-Specific Patterns

### Frontmatter Parsing Edge Cases
The parser handles **trailing whitespace** on closing delimiters - `"---  "` vs `"---"` by trimming before comparison:
```clojure
(filter #(= "---" (str/trim (second %))))  ; Not just (= "---" (second %))
```

### Repository Content Structure
- `awesome-copilot-main/instructions/` - GitHub Copilot instruction files
- `awesome-copilot-main/prompts/` - Prompt files
- `awesome-copilot-main/chatmodes/` - Chat mode configurations (.chatmode.md files)
- `awesome-copilot-main/agents/` - Agent configurations (.agent.md files)
- `awesome-cursorrules-main/rules/` - Technology-specific cursor rules by directory

### Index Data Transformation
Each markdown file becomes:
```clojure
{:filename "file.md"
 :title "Extracted from H1 or generated from filename"
 :description "From frontmatter or nil"
 :link "relative/path/to/file"}
```

Title extraction priority: frontmatter `:title` → H1 heading → filename transformation

### Joyride Integration Architecture
- **ClojureScript script** in `site/awesome_copilot.cljs`
- **Fetches live index** from `INDEX-URL` (GitHub Pages)
- **Preference persistence** using VS Code's globalState API
- **File operations** write to VS Code's user configuration directory

## Code Style Preferences

- **Favor functional programming** approaches that transform data over imperative code
- **Use Babashka's native libraries** (fs, http-client, etc.) instead of shell commands when possible
- **Prefer destructuring** for accessing data but avoid excessive nesting
- **Use meaningful constants** for values that might be reused
- **Prefer code clarity over comments**. Instead of sprinkling comments all over, make the code clear in intent and easy to read, from function and variable names.
- **Include informative logging/println** statements to show task progress
- **Handle cleanup properly** by checking for and removing existing files/directories before operations
- **Provide graceful fallbacks** for data extraction (e.g., generate titles from filenames when H1 headings are missing)

## Technical Considerations

- This is a Babashka project with tasks defined in `src/pez/tasks.clj`
- Prefer `babashka.fs` functions for file operations over shell commands
- Use `fs/unzip` for archive extraction instead of external unzip commands
- When streaming data (like downloading files), use proper resource management with `with-open`
- For HTTP operations, use `babashka.http-client` over shell commands
- Use `cheshire.core` for JSON generation with pretty printing
- Consider idempotency - tasks should be safely runnable multiple times
- Each function should do one clear task, with appropriate logging
- When extracting GitHub repositories, prefer:
  1. Direct ZIP downloads with `babashka.http-client` (preferred for simple content extraction)
  2. Shallow git clones with `.git` directory removal (when git-specific features needed)
- Strive for functional core, imperative shell

## REPL Development Guidelines

- Always start development in the REPL namespace: `(in-ns 'target.namespace)`
- Test individual functions before composing them
- Build up complex transformations step by step
- Show intermediate results to verify data transformations
- Use `require :reload` when testing updated code
- Keep functions pure and composable for easy REPL testing
- **Define helper functions before they are called** - Clojure requires functions to be defined before use (no forward declarations)

## Common Clojure Gotchas

- **Function definition order matters** - Always define helper functions before the functions that call them
- **Namespace evaluation order** - When building up code in the REPL, ensure dependencies are loaded first
- **Structural editing** - Use Calva's backseat driver structural editing tools for cleaner, balanced code rather than manual bracket balancing

Remember: Focus on data transformations, use Babashka's built-in libraries, and follow Clojure's functional programming style. Think: What would Rich Hickey do? Build simple, composable functions that transform data clearly and predictably.

## Common Development Mistakes to Avoid

### File and Directory Management
- **ALWAYS verify file locations and working directories** - Use `pwd` and `ls` commands to confirm context before file operations
- **Check if files exist before attempting operations** - Use tools to verify file structure, especially for binary files
- **Working directory awareness** - Jekyll must be run from the `site/` subdirectory, not the project root
- **Binary file handling** - Never use `create_file` for binary data (images, etc.). Use proper binary file creation methods or manual saving

### Template vs Generated File Confusion
- **Identify source vs generated files** - Look for template files (e.g., `index-template.md`) vs generated files (e.g., `index.md`)
- **Edit the source, not the output** - Always edit template files, not the generated artifacts
- **Understand build processes** - Check for build tasks (e.g., `bb generate-index`) that transform templates into output files
- **Git tracking awareness** - If changes don't show in git, you may be editing generated files
- **Template development workflow** - When editing `index-template.md`, always run `bb generate-index` to regenerate `index.md` with real data

### CSS and Web Development
- **Read existing CSS before modifications** - Always examine current styles to understand existing patterns
- **Avoid CSS property conflicts** - Don't combine `display: block` with `vertical-align` properties
- **Mobile-first responsive design** - Consider all screen sizes, not just desktop
- **Progressive enhancement** - Start with simple solutions and enhance for specific needs

### Jekyll and Static Site Issues
- **Jekyll plugin loading** - Restart Jekyll server after adding new plugins to `_plugins/` directory
- **File exclusion** - Add template files to Jekyll's `exclude` list to prevent them from being published as separate pages
- **Path resolution** - Use Jekyll's `relative_url` filter for proper path handling in different environments

### Process and Communication
- **Break down complex tasks** - Use todo lists for multi-step operations to track progress
- **One change at a time** - Make incremental changes rather than large complex modifications
- **Verify before proceeding** - Check each step works before moving to the next
- **Read error messages carefully** - CSS linting errors and terminal output provide valuable debugging information

### Technical Debt Prevention
- **Complete implementations** - Don't leave half-finished features (e.g., favicon setup without proper binary files)
- **Clean up unused code** - Remove obsolete CSS rules and HTML elements when replacing functionality
- **Consistent patterns** - Follow established project patterns rather than introducing new approaches mid-stream

### Testing and Validation
- **Test on target platforms** - Mobile layouts should be tested on actual mobile screen sizes
- **Validate HTML/CSS** - Check for linting errors and validation issues
- **Cross-browser compatibility** - Consider how changes affect different browsers and devices
- **Live reload verification** - Ensure development servers are running and updating correctly

Remember: **Measure twice, cut once** - Always understand the current state before making changes, and verify each change works before proceeding to the next step.
