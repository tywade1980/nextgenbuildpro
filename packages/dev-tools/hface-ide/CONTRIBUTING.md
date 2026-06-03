# Contributing to Hugging Face IDE

Thank you for your interest in contributing to the Hugging Face IDE extension! This document provides guidelines for contributing to the project.

## Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/Hface-IDE.git
   cd Hface-IDE
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Build the Project**
   ```bash
   npm run compile
   ```

4. **Test in VS Code**
   - Open the project in VS Code
   - Press `F5` to start debugging
   - This will open a new VS Code window with the extension loaded

## Project Structure

```
src/
├── extension.ts              # Main extension entry point
├── types/                   # TypeScript type definitions
│   └── index.ts
├── services/               # Core services
│   └── huggingFaceApi.ts   # Hugging Face API integration
└── providers/              # VS Code providers
    ├── explorerProvider.ts     # Sidebar explorer
    ├── modelSearchProvider.ts  # Model search functionality
    ├── datasetSearchProvider.ts # Dataset search functionality
    └── codeSnippetProvider.ts  # Code snippet generation
```

## Code Style

- Use TypeScript for all new code
- Follow existing naming conventions
- Add JSDoc comments for public methods
- Use meaningful variable and function names
- Keep functions focused and small

## Submitting Changes

1. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Your Changes**
   - Write clean, well-documented code
   - Add tests if applicable
   - Update README if needed

3. **Test Your Changes**
   - Compile the extension: `npm run compile`
   - Test in VS Code development mode
   - Verify all features work as expected

4. **Commit and Push**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   git push origin feature/your-feature-name
   ```

5. **Create Pull Request**
   - Open a pull request against the main branch
   - Provide a clear description of changes
   - Include screenshots for UI changes

## Types of Contributions

### Bug Fixes
- Fix existing functionality that doesn't work correctly
- Improve error handling
- Performance improvements

### New Features
- Additional Hugging Face integrations
- New code snippet templates
- UI/UX improvements
- Support for additional programming languages

### Documentation
- Improve README
- Add code comments
- Create usage examples
- Write tutorials

## Development Guidelines

### Adding New Model Tasks
1. Add the task type to the enum in `package.json`
2. Create task-specific code snippets in `codeSnippetProvider.ts`
3. Update the model search provider if needed
4. Test with actual models from that task category

### Adding New Code Templates
1. Add template functions to `codeSnippetProvider.ts`
2. Follow existing patterns for consistency
3. Include both basic and advanced usage examples
4. Test generated code works correctly

### API Integration
- Always handle network errors gracefully
- Respect rate limits
- Use the configured API token when available
- Cache responses when appropriate

## Testing

Currently testing is done manually. Future plans include:
- Unit tests for core functionality
- Integration tests with Hugging Face API
- UI tests for VS Code components

## Reporting Issues

When reporting bugs or requesting features:

1. Check existing issues first
2. Use issue templates when available
3. Provide detailed reproduction steps
4. Include VS Code version and extension version
5. Add screenshots for UI issues

## Community

- Be respectful and inclusive
- Help others learn and contribute
- Share knowledge and best practices
- Give constructive feedback

## Questions?

Feel free to open an issue with the "question" label if you need help or clarification on anything related to contributing.

Thank you for contributing to the Hugging Face IDE!