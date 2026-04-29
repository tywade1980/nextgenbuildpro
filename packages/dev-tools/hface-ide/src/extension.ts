import * as vscode from 'vscode';
import { HuggingFaceExplorerProvider } from './providers/explorerProvider';
import { HuggingFaceApi } from './services/huggingFaceApi';
import { ModelSearchProvider } from './providers/modelSearchProvider';
import { DatasetSearchProvider } from './providers/datasetSearchProvider';
import { CodeSnippetProvider } from './providers/codeSnippetProvider';

export function activate(context: vscode.ExtensionContext) {
    console.log('Hugging Face IDE extension is now active!');

    // Initialize services
    const hfApi = new HuggingFaceApi();
    
    // Initialize providers
    const explorerProvider = new HuggingFaceExplorerProvider(hfApi);
    const modelSearchProvider = new ModelSearchProvider(hfApi);
    const datasetSearchProvider = new DatasetSearchProvider(hfApi);
    const codeSnippetProvider = new CodeSnippetProvider();

    // Register tree data provider
    vscode.window.createTreeView('hface-explorer', {
        treeDataProvider: explorerProvider,
        showCollapseAll: true
    });

    // Register commands
    const commands = [
        vscode.commands.registerCommand('hface-ide.searchModels', () => {
            modelSearchProvider.showModelSearch();
        }),
        
        vscode.commands.registerCommand('hface-ide.searchDatasets', () => {
            datasetSearchProvider.showDatasetSearch();
        }),
        
        vscode.commands.registerCommand('hface-ide.downloadModel', async (model?: any) => {
            if (model) {
                await modelSearchProvider.downloadModel(model);
            } else {
                vscode.window.showErrorMessage('No model selected');
            }
        }),
        
        vscode.commands.registerCommand('hface-ide.downloadDataset', async (dataset?: any) => {
            if (dataset) {
                await datasetSearchProvider.downloadDataset(dataset);
            } else {
                vscode.window.showErrorMessage('No dataset selected');
            }
        }),
        
        vscode.commands.registerCommand('hface-ide.openHub', () => {
            vscode.env.openExternal(vscode.Uri.parse('https://huggingface.co'));
        }),
        
        vscode.commands.registerCommand('hface-ide.refreshExplorer', () => {
            explorerProvider.refresh();
        }),
        
        vscode.commands.registerCommand('hface-ide.configureApi', async () => {
            const token = await vscode.window.showInputBox({
                prompt: 'Enter your Hugging Face API token',
                placeHolder: 'hf_...',
                password: true
            });
            
            if (token) {
                await vscode.workspace.getConfiguration('hface-ide').update('apiToken', token, vscode.ConfigurationTarget.Global);
                vscode.window.showInformationMessage('API token configured successfully');
                explorerProvider.refresh();
            }
        }),
        
        vscode.commands.registerCommand('hface-ide.insertCodeSnippet', async (model?: any) => {
            if (model) {
                await codeSnippetProvider.insertCodeSnippet(model);
            } else {
                vscode.window.showErrorMessage('No model selected');
            }
        })
    ];

    // Add all commands to subscriptions
    commands.forEach(command => context.subscriptions.push(command));

    // Register additional providers
    context.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            { scheme: 'file', language: 'python' },
            codeSnippetProvider,
            '.'
        )
    );

    // Show welcome message
    vscode.window.showInformationMessage('Hugging Face IDE is ready! Use the command palette or explorer to get started.');
}

export function deactivate() {
    console.log('Hugging Face IDE extension is now deactivated');
}