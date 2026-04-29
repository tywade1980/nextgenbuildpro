import axios, { AxiosInstance } from 'axios';
import * as vscode from 'vscode';
import { HuggingFaceModel, HuggingFaceDataset, SearchFilters, HuggingFaceSpace } from '../types';

export class HuggingFaceApi {
    private client: AxiosInstance;
    private baseUrl = 'https://huggingface.co/api';

    constructor() {
        this.client = axios.create({
            baseURL: this.baseUrl,
            timeout: 30000,
            headers: {
                'User-Agent': 'HuggingFace-IDE-VSCode/0.1.0'
            }
        });

        // Update headers when API token changes
        this.updateAuthHeader();
    }

    private updateAuthHeader() {
        const config = vscode.workspace.getConfiguration('hface-ide');
        const token = config.get<string>('apiToken');
        
        if (token) {
            this.client.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        } else {
            delete this.client.defaults.headers.common['Authorization'];
        }
    }

    async searchModels(query: string = '', filters: SearchFilters = {}): Promise<HuggingFaceModel[]> {
        this.updateAuthHeader();
        
        try {
            const params = new URLSearchParams();
            
            if (query) {
                params.append('search', query);
            }
            
            if (filters.task) {
                params.append('pipeline_tag', filters.task);
            }
            
            if (filters.library) {
                params.append('library', filters.library);
            }
            
            if (filters.language) {
                params.append('language', filters.language);
            }
            
            if (filters.license) {
                params.append('license', filters.license);
            }
            
            if (filters.sort) {
                params.append('sort', filters.sort);
            }
            
            if (filters.direction) {
                params.append('direction', filters.direction);
            }
            
            const limit = filters.limit || vscode.workspace.getConfiguration('hface-ide').get<number>('maxResults', 20);
            params.append('limit', limit.toString());

            const response = await this.client.get(`/models?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('Error searching models:', error);
            vscode.window.showErrorMessage(`Failed to search models: ${error}`);
            return [];
        }
    }

    async searchDatasets(query: string = '', filters: SearchFilters = {}): Promise<HuggingFaceDataset[]> {
        this.updateAuthHeader();
        
        try {
            const params = new URLSearchParams();
            
            if (query) {
                params.append('search', query);
            }
            
            if (filters.task) {
                params.append('task_categories', filters.task);
            }
            
            if (filters.language) {
                params.append('language', filters.language);
            }
            
            if (filters.license) {
                params.append('license', filters.license);
            }
            
            if (filters.sort) {
                params.append('sort', filters.sort);
            }
            
            if (filters.direction) {
                params.append('direction', filters.direction);
            }
            
            const limit = filters.limit || vscode.workspace.getConfiguration('hface-ide').get<number>('maxResults', 20);
            params.append('limit', limit.toString());

            const response = await this.client.get(`/datasets?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('Error searching datasets:', error);
            vscode.window.showErrorMessage(`Failed to search datasets: ${error}`);
            return [];
        }
    }

    async getModelInfo(modelId: string): Promise<HuggingFaceModel | null> {
        this.updateAuthHeader();
        
        try {
            const response = await this.client.get(`/models/${modelId}`);
            return response.data;
        } catch (error) {
            console.error('Error getting model info:', error);
            return null;
        }
    }

    async getDatasetInfo(datasetId: string): Promise<HuggingFaceDataset | null> {
        this.updateAuthHeader();
        
        try {
            const response = await this.client.get(`/datasets/${datasetId}`);
            return response.data;
        } catch (error) {
            console.error('Error getting dataset info:', error);
            return null;
        }
    }

    async getPopularModels(task?: string): Promise<HuggingFaceModel[]> {
        return this.searchModels('', {
            task,
            sort: 'downloads',
            direction: 'desc',
            limit: 10
        });
    }

    async getPopularDatasets(): Promise<HuggingFaceDataset[]> {
        return this.searchDatasets('', {
            sort: 'downloads',
            direction: 'desc',
            limit: 10
        });
    }

    async getTrendingModels(): Promise<HuggingFaceModel[]> {
        return this.searchModels('', {
            sort: 'likes',
            direction: 'desc',
            limit: 10
        });
    }

    async searchSpaces(query: string = ''): Promise<HuggingFaceSpace[]> {
        this.updateAuthHeader();
        
        try {
            const params = new URLSearchParams();
            
            if (query) {
                params.append('search', query);
            }
            
            params.append('limit', '20');

            const response = await this.client.get(`/spaces?${params.toString()}`);
            return response.data;
        } catch (error) {
            console.error('Error searching spaces:', error);
            vscode.window.showErrorMessage(`Failed to search spaces: ${error}`);
            return [];
        }
    }

    getModelUrl(modelId: string): string {
        return `https://huggingface.co/${modelId}`;
    }

    getDatasetUrl(datasetId: string): string {
        return `https://huggingface.co/datasets/${datasetId}`;
    }

    getSpaceUrl(spaceId: string): string {
        return `https://huggingface.co/spaces/${spaceId}`;
    }
}