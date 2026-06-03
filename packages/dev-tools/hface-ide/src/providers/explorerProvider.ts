import * as vscode from 'vscode';
import { HuggingFaceApi } from '../services/huggingFaceApi';
import { HuggingFaceModel, HuggingFaceDataset } from '../types';

export class HuggingFaceExplorerProvider implements vscode.TreeDataProvider<ExplorerItem> {
    private _onDidChangeTreeData: vscode.EventEmitter<ExplorerItem | undefined | null | void> = new vscode.EventEmitter<ExplorerItem | undefined | null | void>();
    readonly onDidChangeTreeData: vscode.Event<ExplorerItem | undefined | null | void> = this._onDidChangeTreeData.event;

    constructor(private hfApi: HuggingFaceApi) {}

    refresh(): void {
        this._onDidChangeTreeData.fire();
    }

    getTreeItem(element: ExplorerItem): vscode.TreeItem {
        return element;
    }

    async getChildren(element?: ExplorerItem): Promise<ExplorerItem[]> {
        if (!element) {
            // Root level
            return [
                new ExplorerItem('Popular Models', vscode.TreeItemCollapsibleState.Collapsed, 'category', 'popular-models'),
                new ExplorerItem('Trending Models', vscode.TreeItemCollapsibleState.Collapsed, 'category', 'trending-models'),
                new ExplorerItem('Popular Datasets', vscode.TreeItemCollapsibleState.Collapsed, 'category', 'popular-datasets'),
                new ExplorerItem('Model Tasks', vscode.TreeItemCollapsibleState.Collapsed, 'category', 'model-tasks'),
                new ExplorerItem('Quick Actions', vscode.TreeItemCollapsibleState.Collapsed, 'category', 'quick-actions')
            ];
        }

        switch (element.contextValue) {
            case 'popular-models':
                return this.getPopularModels();
            case 'trending-models':
                return this.getTrendingModels();
            case 'popular-datasets':
                return this.getPopularDatasets();
            case 'model-tasks':
                return this.getModelTasks();
            case 'quick-actions':
                return this.getQuickActions();
            case 'task':
                return this.getTaskModels(element.id || '');
            default:
                return [];
        }
    }

    private async getPopularModels(): Promise<ExplorerItem[]> {
        try {
            const models = await this.hfApi.getPopularModels();
            return models.map(model => new ModelItem(model));
        } catch (error) {
            return [new ExplorerItem('Error loading models', vscode.TreeItemCollapsibleState.None, 'error')];
        }
    }

    private async getTrendingModels(): Promise<ExplorerItem[]> {
        try {
            const models = await this.hfApi.getTrendingModels();
            return models.map(model => new ModelItem(model));
        } catch (error) {
            return [new ExplorerItem('Error loading models', vscode.TreeItemCollapsibleState.None, 'error')];
        }
    }

    private async getPopularDatasets(): Promise<ExplorerItem[]> {
        try {
            const datasets = await this.hfApi.getPopularDatasets();
            return datasets.map(dataset => new DatasetItem(dataset));
        } catch (error) {
            return [new ExplorerItem('Error loading datasets', vscode.TreeItemCollapsibleState.None, 'error')];
        }
    }

    private getModelTasks(): ExplorerItem[] {
        const tasks = [
            'text-generation',
            'text-classification',
            'token-classification',
            'question-answering',
            'fill-mask',
            'summarization',
            'translation',
            'text2text-generation',
            'image-classification',
            'object-detection',
            'image-segmentation',
            'audio-classification',
            'automatic-speech-recognition',
            'text-to-speech'
        ];

        return tasks.map(task => new ExplorerItem(
            task.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase()),
            vscode.TreeItemCollapsibleState.Collapsed,
            'task',
            task
        ));
    }

    private async getTaskModels(task: string): Promise<ExplorerItem[]> {
        try {
            const models = await this.hfApi.getPopularModels(task);
            return models.slice(0, 5).map(model => new ModelItem(model));
        } catch (error) {
            return [new ExplorerItem('Error loading models', vscode.TreeItemCollapsibleState.None, 'error')];
        }
    }

    private getQuickActions(): ExplorerItem[] {
        return [
            new QuickActionItem('Search Models', 'hface-ide.searchModels', '$(search)'),
            new QuickActionItem('Search Datasets', 'hface-ide.searchDatasets', '$(database)'),
            new QuickActionItem('Open Hugging Face Hub', 'hface-ide.openHub', '$(globe)'),
            new QuickActionItem('Configure API Token', 'hface-ide.configureApi', '$(key)')
        ];
    }
}

export class ExplorerItem extends vscode.TreeItem {
    constructor(
        public readonly label: string,
        public readonly collapsibleState: vscode.TreeItemCollapsibleState,
        public readonly contextValue: string,
        public readonly id?: string
    ) {
        super(label, collapsibleState);
        this.id = id || label;
        this.tooltip = this.label;
    }
}

export class ModelItem extends ExplorerItem {
    constructor(public readonly model: HuggingFaceModel) {
        super(
            model.id,
            vscode.TreeItemCollapsibleState.None,
            'model',
            model.id
        );
        
        this.description = `↓${model.downloads} ♡${model.likes}`;
        this.tooltip = `${model.id}\nDownloads: ${model.downloads}\nLikes: ${model.likes}\nTask: ${model.pipeline_tag || 'Unknown'}`;
        
        if (model.pipeline_tag) {
            this.iconPath = new vscode.ThemeIcon('robot');
        } else {
            this.iconPath = new vscode.ThemeIcon('file');
        }
        
        this.command = {
            command: 'vscode.open',
            title: 'Open Model',
            arguments: [vscode.Uri.parse(this.getModelUrl())]
        };
    }

    private getModelUrl(): string {
        return `https://huggingface.co/${this.model.id}`;
    }
}

export class DatasetItem extends ExplorerItem {
    constructor(public readonly dataset: HuggingFaceDataset) {
        super(
            dataset.id,
            vscode.TreeItemCollapsibleState.None,
            'dataset',
            dataset.id
        );
        
        this.description = `↓${dataset.downloads} ♡${dataset.likes}`;
        this.tooltip = `${dataset.id}\nDownloads: ${dataset.downloads}\nLikes: ${dataset.likes}`;
        this.iconPath = new vscode.ThemeIcon('database');
        
        this.command = {
            command: 'vscode.open',
            title: 'Open Dataset',
            arguments: [vscode.Uri.parse(this.getDatasetUrl())]
        };
    }

    private getDatasetUrl(): string {
        return `https://huggingface.co/datasets/${this.dataset.id}`;
    }
}

export class QuickActionItem extends ExplorerItem {
    constructor(
        label: string,
        private readonly commandId: string,
        private readonly icon: string
    ) {
        super(label, vscode.TreeItemCollapsibleState.None, 'action');
        
        this.iconPath = new vscode.ThemeIcon(icon.replace('$(', '').replace(')', ''));
        this.command = {
            command: this.commandId,
            title: label
        };
    }
}