import * as vscode from 'vscode';
import * as fs from 'fs-extra';
import * as path from 'path';
import { HuggingFaceApi } from '../services/huggingFaceApi';
import { HuggingFaceModel, SearchFilters } from '../types';

export class ModelSearchProvider {
    constructor(private hfApi: HuggingFaceApi) {}

    async showModelSearch(): Promise<void> {
        const query = await vscode.window.showInputBox({
            prompt: 'Search for models on Hugging Face',
            placeHolder: 'Enter search terms (e.g., "text classification", "bert", "gpt")'
        });

        if (!query) {
            return;
        }

        try {
            vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `Searching for models: ${query}`,
                cancellable: false
            }, async () => {
                const models = await this.hfApi.searchModels(query);
                await this.showModelResults(models, query);
            });
        } catch (error) {
            vscode.window.showErrorMessage(`Search failed: ${error}`);
        }
    }

    private async showModelResults(models: HuggingFaceModel[], query: string): Promise<void> {
        if (models.length === 0) {
            vscode.window.showInformationMessage(`No models found for "${query}"`);
            return;
        }

        const items = models.map(model => ({
            label: model.id,
            description: `$(cloud-download) ${model.downloads} $(heart) ${model.likes}`,
            detail: `Task: ${model.pipeline_tag || 'Unknown'} | Library: ${model.library_name || 'Unknown'}`,
            model: model
        }));

        const selected = await vscode.window.showQuickPick(items, {
            placeHolder: `Found ${models.length} models for "${query}". Select one to view options:`,
            matchOnDescription: true,
            matchOnDetail: true
        });

        if (selected) {
            await this.showModelActions(selected.model);
        }
    }

    private async showModelActions(model: HuggingFaceModel): Promise<void> {
        const actions = [
            {
                label: '$(globe) Open in Browser',
                description: 'View model page on Hugging Face Hub',
                action: 'open'
            },
            {
                label: '$(cloud-download) Download Model',
                description: 'Download model files to local cache',
                action: 'download'
            },
            {
                label: '$(code) Insert Code Snippet',
                description: 'Insert code to use this model',
                action: 'snippet'
            },
            {
                label: '$(info) View Model Info',
                description: 'Show detailed model information',
                action: 'info'
            },
            {
                label: '$(copy) Copy Model ID',
                description: 'Copy model identifier to clipboard',
                action: 'copy'
            }
        ];

        const selected = await vscode.window.showQuickPick(actions, {
            placeHolder: `Actions for ${model.id}:`
        });

        if (!selected) {
            return;
        }

        switch (selected.action) {
            case 'open':
                vscode.env.openExternal(vscode.Uri.parse(this.hfApi.getModelUrl(model.id)));
                break;
            case 'download':
                await this.downloadModel(model);
                break;
            case 'snippet':
                await this.insertCodeSnippet(model);
                break;
            case 'info':
                await this.showModelInfo(model);
                break;
            case 'copy':
                vscode.env.clipboard.writeText(model.id);
                vscode.window.showInformationMessage(`Copied "${model.id}" to clipboard`);
                break;
        }
    }

    async downloadModel(model: HuggingFaceModel): Promise<void> {
        const config = vscode.workspace.getConfiguration('hface-ide');
        const cacheDir = config.get<string>('cacheDirectory', '~/.cache/huggingface');
        const expandedCacheDir = cacheDir.replace('~', require('os').homedir());
        const modelDir = path.join(expandedCacheDir, 'models', model.id);

        try {
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `Downloading ${model.id}`,
                cancellable: false
            }, async (progress) => {
                progress.report({ message: 'Creating directory...' });
                await fs.ensureDir(modelDir);

                // Create a simple download script that can be run by the user
                const downloadScript = this.generateDownloadScript(model, modelDir);
                const scriptPath = path.join(modelDir, 'download.py');
                await fs.writeFile(scriptPath, downloadScript);

                progress.report({ message: 'Created download script' });
                
                vscode.window.showInformationMessage(
                    `Download script created at ${scriptPath}. Run it to download the model files.`,
                    'Open Folder',
                    'View Script'
                ).then(selection => {
                    if (selection === 'Open Folder') {
                        vscode.commands.executeCommand('vscode.openFolder', vscode.Uri.file(modelDir), true);
                    } else if (selection === 'View Script') {
                        vscode.workspace.openTextDocument(scriptPath).then(doc => {
                            vscode.window.showTextDocument(doc);
                        });
                    }
                });
            });
        } catch (error) {
            vscode.window.showErrorMessage(`Failed to prepare download: ${error}`);
        }
    }

    private generateDownloadScript(model: HuggingFaceModel, targetDir: string): string {
        return `#!/usr/bin/env python3
"""
Download script for ${model.id}
Generated by Hugging Face IDE Extension
"""

import os
from huggingface_hub import snapshot_download

def main():
    model_id = "${model.id}"
    cache_dir = "${targetDir}"
    
    print(f"Downloading {model_id}...")
    print(f"Target directory: {cache_dir}")
    
    try:
        snapshot_download(
            repo_id=model_id,
            cache_dir=cache_dir,
            resume_download=True
        )
        print(f"✓ Successfully downloaded {model_id}")
    except Exception as e:
        print(f"✗ Error downloading {model_id}: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
`;
    }

    private async insertCodeSnippet(model: HuggingFaceModel): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active editor found');
            return;
        }

        const snippets = this.generateCodeSnippets(model);
        const selectedSnippet = await vscode.window.showQuickPick(
            snippets.map(s => ({
                label: s.description,
                description: s.language,
                snippet: s
            })),
            { placeHolder: 'Select a code snippet to insert:' }
        );

        if (selectedSnippet) {
            const position = editor.selection.active;
            await editor.edit(editBuilder => {
                editBuilder.insert(position, selectedSnippet.snippet.code);
            });
        }
    }

    private generateCodeSnippets(model: HuggingFaceModel) {
        const snippets = [];
        const modelId = model.id;
        const task = model.pipeline_tag;

        // Basic usage snippet
        if (task === 'text-generation') {
            snippets.push({
                language: 'Python',
                description: 'Basic text generation',
                code: `from transformers import pipeline

# Load the model
generator = pipeline('text-generation', model='${modelId}')

# Generate text
result = generator("Your prompt here", max_length=100, num_return_sequences=1)
print(result[0]['generated_text'])
`
            });
        } else if (task === 'text-classification') {
            snippets.push({
                language: 'Python',
                description: 'Text classification',
                code: `from transformers import pipeline

# Load the model
classifier = pipeline('text-classification', model='${modelId}')

# Classify text
result = classifier("Your text here")
print(result)
`
            });
        } else {
            snippets.push({
                language: 'Python',
                description: 'Generic model usage',
                code: `from transformers import pipeline

# Load the model
model = pipeline('${task || 'feature-extraction'}', model='${modelId}')

# Use the model
result = model("Your input here")
print(result)
`
            });
        }

        // Advanced usage with AutoModel
        snippets.push({
            language: 'Python',
            description: 'Advanced usage with AutoModel',
            code: `from transformers import AutoTokenizer, AutoModel

# Load tokenizer and model
tokenizer = AutoTokenizer.from_pretrained('${modelId}')
model = AutoModel.from_pretrained('${modelId}')

# Tokenize input
inputs = tokenizer("Your text here", return_tensors="pt")

# Get model outputs
outputs = model(**inputs)
print(outputs.last_hidden_state.shape)
`
        });

        return snippets;
    }

    private async showModelInfo(model: HuggingFaceModel): Promise<void> {
        const info = `
**Model ID:** ${model.id}
**Task:** ${model.pipeline_tag || 'Unknown'}
**Library:** ${model.library_name || 'Unknown'}
**Downloads:** ${model.downloads}
**Likes:** ${model.likes}
**Created:** ${new Date(model.created_at).toLocaleDateString()}
**Last Modified:** ${new Date(model.last_modified).toLocaleDateString()}
**Private:** ${model.private ? 'Yes' : 'No'}
**Tags:** ${model.tags.join(', ')}

**Files:**
${model.siblings?.map(s => `- ${s.rfilename}`).join('\n') || 'No file information available'}
        `.trim();

        const document = await vscode.workspace.openTextDocument({
            content: info,
            language: 'markdown'
        });

        await vscode.window.showTextDocument(document);
    }
}