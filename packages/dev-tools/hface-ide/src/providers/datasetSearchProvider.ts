import * as vscode from 'vscode';
import * as fs from 'fs-extra';
import * as path from 'path';
import { HuggingFaceApi } from '../services/huggingFaceApi';
import { HuggingFaceDataset, SearchFilters } from '../types';

export class DatasetSearchProvider {
    constructor(private hfApi: HuggingFaceApi) {}

    async showDatasetSearch(): Promise<void> {
        const query = await vscode.window.showInputBox({
            prompt: 'Search for datasets on Hugging Face',
            placeHolder: 'Enter search terms (e.g., "text classification", "translation", "image")'
        });

        if (!query) {
            return;
        }

        try {
            vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `Searching for datasets: ${query}`,
                cancellable: false
            }, async () => {
                const datasets = await this.hfApi.searchDatasets(query);
                await this.showDatasetResults(datasets, query);
            });
        } catch (error) {
            vscode.window.showErrorMessage(`Search failed: ${error}`);
        }
    }

    private async showDatasetResults(datasets: HuggingFaceDataset[], query: string): Promise<void> {
        if (datasets.length === 0) {
            vscode.window.showInformationMessage(`No datasets found for "${query}"`);
            return;
        }

        const items = datasets.map(dataset => ({
            label: dataset.id,
            description: `$(cloud-download) ${dataset.downloads} $(heart) ${dataset.likes}`,
            detail: dataset.description || 'No description available',
            dataset: dataset
        }));

        const selected = await vscode.window.showQuickPick(items, {
            placeHolder: `Found ${datasets.length} datasets for "${query}". Select one to view options:`,
            matchOnDescription: true,
            matchOnDetail: true
        });

        if (selected) {
            await this.showDatasetActions(selected.dataset);
        }
    }

    private async showDatasetActions(dataset: HuggingFaceDataset): Promise<void> {
        const actions = [
            {
                label: '$(globe) Open in Browser',
                description: 'View dataset page on Hugging Face Hub',
                action: 'open'
            },
            {
                label: '$(cloud-download) Download Dataset',
                description: 'Download dataset files to local cache',
                action: 'download'
            },
            {
                label: '$(code) Insert Code Snippet',
                description: 'Insert code to load this dataset',
                action: 'snippet'
            },
            {
                label: '$(info) View Dataset Info',
                description: 'Show detailed dataset information',
                action: 'info'
            },
            {
                label: '$(copy) Copy Dataset ID',
                description: 'Copy dataset identifier to clipboard',
                action: 'copy'
            }
        ];

        const selected = await vscode.window.showQuickPick(actions, {
            placeHolder: `Actions for ${dataset.id}:`
        });

        if (!selected) {
            return;
        }

        switch (selected.action) {
            case 'open':
                vscode.env.openExternal(vscode.Uri.parse(this.hfApi.getDatasetUrl(dataset.id)));
                break;
            case 'download':
                await this.downloadDataset(dataset);
                break;
            case 'snippet':
                await this.insertCodeSnippet(dataset);
                break;
            case 'info':
                await this.showDatasetInfo(dataset);
                break;
            case 'copy':
                vscode.env.clipboard.writeText(dataset.id);
                vscode.window.showInformationMessage(`Copied "${dataset.id}" to clipboard`);
                break;
        }
    }

    async downloadDataset(dataset: HuggingFaceDataset): Promise<void> {
        const config = vscode.workspace.getConfiguration('hface-ide');
        const cacheDir = config.get<string>('cacheDirectory', '~/.cache/huggingface');
        const expandedCacheDir = cacheDir.replace('~', require('os').homedir());
        const datasetDir = path.join(expandedCacheDir, 'datasets', dataset.id);

        try {
            await vscode.window.withProgress({
                location: vscode.ProgressLocation.Notification,
                title: `Downloading ${dataset.id}`,
                cancellable: false
            }, async (progress) => {
                progress.report({ message: 'Creating directory...' });
                await fs.ensureDir(datasetDir);

                // Create a download script
                const downloadScript = this.generateDownloadScript(dataset, datasetDir);
                const scriptPath = path.join(datasetDir, 'download.py');
                await fs.writeFile(scriptPath, downloadScript);

                progress.report({ message: 'Created download script' });
                
                vscode.window.showInformationMessage(
                    `Download script created at ${scriptPath}. Run it to download the dataset files.`,
                    'Open Folder',
                    'View Script'
                ).then(selection => {
                    if (selection === 'Open Folder') {
                        vscode.commands.executeCommand('vscode.openFolder', vscode.Uri.file(datasetDir), true);
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

    private generateDownloadScript(dataset: HuggingFaceDataset, targetDir: string): string {
        return `#!/usr/bin/env python3
"""
Download script for ${dataset.id}
Generated by Hugging Face IDE Extension
"""

import os
from datasets import load_dataset

def main():
    dataset_id = "${dataset.id}"
    cache_dir = "${targetDir}"
    
    print(f"Downloading {dataset_id}...")
    print(f"Target directory: {cache_dir}")
    
    try:
        # Load and cache the dataset
        dataset = load_dataset(dataset_id, cache_dir=cache_dir)
        
        print(f"✓ Successfully downloaded {dataset_id}")
        print(f"Dataset info:")
        print(f"  - Splits: {list(dataset.keys())}")
        for split_name, split_data in dataset.items():
            print(f"  - {split_name}: {len(split_data)} examples")
            if len(split_data) > 0:
                print(f"    Features: {list(split_data.features.keys())}")
                
    except Exception as e:
        print(f"✗ Error downloading {dataset_id}: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
`;
    }

    private async insertCodeSnippet(dataset: HuggingFaceDataset): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active editor found');
            return;
        }

        const snippets = this.generateCodeSnippets(dataset);
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

    private generateCodeSnippets(dataset: HuggingFaceDataset) {
        const snippets = [];
        const datasetId = dataset.id;

        // Basic loading snippet
        snippets.push({
            language: 'Python',
            description: 'Load dataset with datasets library',
            code: `from datasets import load_dataset

# Load the dataset
dataset = load_dataset('${datasetId}')

# Print dataset info
print(dataset)

# Access train split (if available)
if 'train' in dataset:
    train_data = dataset['train']
    print(f"Training examples: {len(train_data)}")
    print(f"Features: {list(train_data.features.keys())}")
    
    # Show first example
    print("First example:")
    print(train_data[0])
`
        });

        // Streaming snippet for large datasets
        snippets.push({
            language: 'Python',
            description: 'Stream dataset (for large datasets)',
            code: `from datasets import load_dataset

# Load dataset in streaming mode
dataset = load_dataset('${datasetId}', streaming=True)

# Iterate through examples
for example in dataset['train'].take(10):
    print(example)
    break  # Remove this to process all examples
`
        });

        // Pandas integration snippet
        snippets.push({
            language: 'Python',
            description: 'Convert to pandas DataFrame',
            code: `from datasets import load_dataset
import pandas as pd

# Load the dataset
dataset = load_dataset('${datasetId}')

# Convert to pandas DataFrame
if 'train' in dataset:
    df = dataset['train'].to_pandas()
    print(f"DataFrame shape: {df.shape}")
    print(df.head())
else:
    # Use first available split
    split_name = list(dataset.keys())[0]
    df = dataset[split_name].to_pandas()
    print(f"DataFrame shape: {df.shape}")
    print(df.head())
`
        });

        // Data preprocessing snippet
        snippets.push({
            language: 'Python',
            description: 'Basic data preprocessing',
            code: `from datasets import load_dataset

# Load the dataset
dataset = load_dataset('${datasetId}')

# Example preprocessing function
def preprocess_function(examples):
    # Customize this function based on your dataset
    # For text data:
    # return tokenizer(examples['text'], truncation=True, padding=True)
    
    # For now, just return the examples as-is
    return examples

# Apply preprocessing
if 'train' in dataset:
    processed_dataset = dataset['train'].map(preprocess_function, batched=True)
    print(f"Processed {len(processed_dataset)} examples")
else:
    split_name = list(dataset.keys())[0]
    processed_dataset = dataset[split_name].map(preprocess_function, batched=True)
    print(f"Processed {len(processed_dataset)} examples")
`
        });

        return snippets;
    }

    private async showDatasetInfo(dataset: HuggingFaceDataset): Promise<void> {
        const info = `
**Dataset ID:** ${dataset.id}
**Downloads:** ${dataset.downloads}
**Likes:** ${dataset.likes}
**Created:** ${new Date(dataset.created_at).toLocaleDateString()}
**Last Modified:** ${new Date(dataset.last_modified).toLocaleDateString()}
**Private:** ${dataset.private ? 'Yes' : 'No'}
**Tags:** ${dataset.tags.join(', ')}

**Description:**
${dataset.description || 'No description available'}

**Files:**
${dataset.siblings?.map(s => `- ${s.rfilename}`).join('\n') || 'No file information available'}
        `.trim();

        const document = await vscode.workspace.openTextDocument({
            content: info,
            language: 'markdown'
        });

        await vscode.window.showTextDocument(document);
    }
}