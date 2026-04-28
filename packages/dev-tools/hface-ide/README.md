# Hugging Face IDE

A comprehensive VS Code extension that provides seamless access to Hugging Face models, datasets, and code resources directly from your IDE.

## Features

### 🤖 Model Integration
- **Search Models**: Browse and search through thousands of models on Hugging Face Hub
- **Quick Download**: Easy model download with generated scripts
- **Code Generation**: Automatic code snippet generation for different tasks
- **Model Information**: Detailed model metadata and usage statistics

### 📊 Dataset Access
- **Dataset Search**: Find and explore datasets for your projects
- **Easy Integration**: Generated code for loading and preprocessing datasets
- **Multiple Formats**: Support for various data formats and streaming
- **Dataset Information**: Comprehensive dataset metadata

### 💡 Code Snippets
- **Task-Specific**: Automatically generated code snippets based on model tasks
- **Multiple Languages**: Support for Python with plans for other languages
- **Best Practices**: Code follows Hugging Face best practices
- **Auto-completion**: IntelliSense support for common patterns

### 🎯 Quick Actions
- **Explorer Panel**: Dedicated sidebar panel for browsing HF resources
- **Command Palette**: All features accessible via VS Code command palette
- **One-Click Access**: Direct links to Hugging Face Hub
- **API Integration**: Optional API token support for private repos

## Installation

1. Install the extension from VS Code Marketplace (coming soon) or manually:
   ```bash
   # Clone and build
   git clone https://github.com/tywade1980/Hface-IDE.git
   cd Hface-IDE
   npm install
   npm run compile
   ```

2. Load the extension in VS Code:
   - Open VS Code
   - Go to Extensions
   - Click "..." → "Install from VSIX"
   - Select the generated .vsix file

## Quick Start

1. **Open the Hugging Face Explorer**: Look for the "Hugging Face" panel in the VS Code Explorer sidebar

2. **Search for Models**: 
   - Use `Ctrl+Shift+P` → "Hugging Face: Search Models"
   - Or click "Search Models" in the explorer panel

3. **Insert Code Snippets**:
   - Select a model from search results
   - Choose "Insert Code Snippet"
   - Select the type of code you want to generate

4. **Configure API Token** (optional):
   - Click the key icon in the explorer panel
   - Enter your Hugging Face API token for private repos and higher rate limits

## Usage Examples

### Text Generation
```python
from transformers import pipeline

# Initialize the text generation pipeline
generator = pipeline('text-generation', model='gpt2')

# Generate text
prompt = "The future of AI is"
result = generator(prompt, max_length=100, num_return_sequences=1)
print(result[0]['generated_text'])
```

### Dataset Loading
```python
from datasets import load_dataset

# Load the dataset
dataset = load_dataset('imdb')

# Print dataset info
print(dataset)
print(f"Training examples: {len(dataset['train'])}")
```

### Image Classification
```python
from transformers import pipeline
from PIL import Image

# Initialize image classification pipeline
classifier = pipeline('image-classification', model='google/vit-base-patch16-224')

# Classify image
image = Image.open('path/to/image.jpg')
result = classifier(image)
print(result)
```

## Features in Detail

### Explorer Panel
The Hugging Face explorer panel provides quick access to:
- **Popular Models**: Most downloaded models by category
- **Trending Models**: Models with most recent activity
- **Popular Datasets**: Most used datasets
- **Model Tasks**: Browse models by ML task type
- **Quick Actions**: Search, configure, and access tools

### Search and Discovery
- **Intelligent Search**: Find models and datasets using natural language
- **Filtering**: Filter by task, library, language, license
- **Sorting**: Sort by downloads, likes, recent updates
- **Pagination**: Browse through large result sets

### Code Generation
Automatically generates code snippets for:
- **Basic Usage**: Simple pipeline implementations
- **Advanced Usage**: AutoModel and AutoTokenizer patterns
- **Batch Processing**: Efficient batch inference code
- **Training**: Model fine-tuning boilerplate
- **Dataset Processing**: Data loading and preprocessing

### Configuration Options
- **API Token**: Configure for private repository access
- **Cache Directory**: Set custom model/dataset cache location
- **Default Task**: Set preferred task type for searches
- **Result Limits**: Control number of search results

## Keyboard Shortcuts

| Command | Shortcut | Description |
|---------|----------|-------------|
| Search Models | `Ctrl+Shift+M` | Open model search |
| Search Datasets | `Ctrl+Shift+D` | Open dataset search |
| Open Hub | `Ctrl+Shift+H` | Open Hugging Face Hub |
| Refresh Explorer | `Ctrl+Shift+R` | Refresh explorer panel |

## Requirements

- VS Code 1.74.0 or higher
- Python environment with `transformers` library (for generated code)
- Internet connection for accessing Hugging Face Hub

## Extension Settings

This extension contributes the following settings:

- `hface-ide.apiToken`: Hugging Face API token for private repositories
- `hface-ide.cacheDirectory`: Directory to cache downloaded models and datasets
- `hface-ide.defaultTask`: Default task type for model searches
- `hface-ide.maxResults`: Maximum number of results to show in searches

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📫 [Report Issues](https://github.com/tywade1980/Hface-IDE/issues)
- 💬 [Join Discussions](https://github.com/tywade1980/Hface-IDE/discussions)
- 📖 [Documentation](https://github.com/tywade1980/Hface-IDE/wiki)

## Roadmap

### Upcoming Features
- [ ] Support for more programming languages (JavaScript, R, Julia)
- [ ] Spaces integration for running demos
- [ ] Model comparison tools
- [ ] Local model testing environment
- [ ] Collaboration features
- [ ] Custom model templates
- [ ] Automated model evaluation
- [ ] Integration with Jupyter notebooks

### Version History
- **v0.1.0**: Initial release with core functionality
  - Model and dataset search
  - Code snippet generation
  - Explorer panel
  - Basic configuration options

---

Made with ❤️ for the Hugging Face community
