# Copilot Instructions for Awesome Copilot Index

## About this Project

This repository is designed to work with and manage the content from GitHub's awesome-copilot repository. It uses Babashka for task automation, following functional and data-oriented principles of the Clojure ecosystem.

Always show what you evaluate in the repl to the user, using a codeblock that starts with the namespace in a `(in-ns ...)` form.

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

## Development Approach

- **Start with REPL-driven development** - Build solutions incrementally in the REPL before writing to files
- **Test each step** - Evaluate functions as you develop them to ensure they work correctly
- **Build data transformation pipelines** - Chain functions that transform data from one shape to another
- **Use functional composition** - Combine simple functions to create complex behaviors
- **Handle edge cases gracefully** - Provide fallbacks and sensible defaults

## Problem Solving Approach

1. Break down the problem into clear data transformation steps
2. Consider available Babashka libraries first before reaching for shell commands
3. Validate operations with small tests in the Babashka REPL
4. Prefer pure functions where possible, isolating side effects
5. Provide clear error messages and progress updates
6. Clean up temporary resources after use
7. Use REPL-driven development to iterate on solutions

## Common Task Patterns

- **File Downloads**: Use `babashka.http-client` with streaming for large files
- **File Operations**: Use `babashka.fs` namespace functions
- **Compression**: Use `fs/unzip` and `fs/zip` for working with archives
- **Resource Management**: Use `with-open` for proper cleanup of resources
- **Git Operations**: For git-specific tasks, use shell commands through `babashka.tasks/shell`
- **Data Processing**: Parse structured data formats and transform between representations
- **String Processing**: Convert between different string formats with proper capitalization and formatting

## Task Function Structure

```clojure
(defn my-task
  "Clear docstring explaining purpose"
  [& args]
  ;; Cleanup of existing artifacts
  (when (fs/exists? some-path)
    (println "Cleaning up...")
    (fs/delete-tree some-path))

  ;; Main operation with clear logging
  (println "Performing main operation...")
  (some-operation)

  ;; Final status message
  (println "Task completed successfully"))
```

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
