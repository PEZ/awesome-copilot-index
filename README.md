# Awesome Copilot Index

An automatically generated index of the [awesome-copilot](https://github.com/github/awesome-copilot) repository, providing structured access to GitHub Copilot instructions, prompts, and chat modes.

## 🚀 Live Index

Visit the live index at: **[GitHub Pages URL]** (will be available after first deployment)

## 📥 Download Index Files

- **JSON Format**: [index.json](index.json)
- **EDN Format**: [index.edn](index.edn)

## 🛠️ Usage

### Download the Repository

```bash
bb download-awesome!
```

### Generate Index Files

```bash
bb generate-index!
```

### Run Both Steps

```bash
bb download-awesome! && bb generate-index!
```

## 📊 Index Structure

The generated index contains three main sections:

- **instructions** - GitHub Copilot instruction files
- **prompts** - Prompt templates and examples
- **chatmodes** - Chat mode configurations

Each entry includes:
- `filename` - Original markdown filename
- `title` - Extracted from H1 heading or generated from filename
- `description` - From frontmatter if available
- `link` - Relative path to the file in the repository

## 🔄 Automation

The index is automatically updated:
- **Daily** at 6 AM UTC
- **On push** to the main branch
- **Manually** via GitHub Actions workflow dispatch

## 🏗️ Built With

- **[Babashka](https://babashka.org/)** - Task automation and data processing
- **[Clojure](https://clojure.org/)** - Functional data transformation
- **GitHub Actions** - Automated builds and deployment
- **GitHub Pages** - Hosting

## 📖 Development

This project follows functional programming principles and REPL-driven development:

1. **Data-oriented design** - Transform data through pure functions
2. **Incremental development** - Build solutions step by step in the REPL
3. **Graceful fallbacks** - Handle missing data elegantly
4. **Idempotent operations** - Safe to run multiple times

### Local Development

```bash
# Install Babashka
# https://babashka.org/

# Clone this repository
git clone https://github.com/pez/awesome-copilot-index.git
cd awesome-copilot-index

# Download and index the awesome-copilot repository
bb download-awesome!
bb generate-index!

# Open index.html to view the results
```

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [awesome-copilot](https://github.com/github/awesome-copilot) - The source repository
- [Babashka](https://babashka.org/) - Making Clojure scripting delightful
- [GitHub](https://github.com) - For Actions and Pages hosting
