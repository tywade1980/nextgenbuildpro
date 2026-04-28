export interface HuggingFaceModel {
    id: string;
    author?: string;
    sha: string;
    created_at: string;
    last_modified: string;
    private: boolean;
    downloads: number;
    likes: number;
    tags: string[];
    pipeline_tag?: string;
    library_name?: string;
    model_index?: any;
    config?: any;
    transformersInfo?: any;
    cardData?: any;
    siblings?: Array<{
        rfilename: string;
    }>;
}

export interface HuggingFaceDataset {
    id: string;
    author?: string;
    sha: string;
    created_at: string;
    last_modified: string;
    private: boolean;
    downloads: number;
    likes: number;
    tags: string[];
    description?: string;
    cardData?: any;
    siblings?: Array<{
        rfilename: string;
    }>;
}

export interface SearchFilters {
    task?: string;
    library?: string;
    language?: string;
    license?: string;
    sort?: 'downloads' | 'likes' | 'updated' | 'created';
    direction?: 'asc' | 'desc';
    limit?: number;
}

export interface HuggingFaceSpace {
    id: string;
    author?: string;
    sha: string;
    created_at: string;
    last_modified: string;
    private: boolean;
    likes: number;
    tags: string[];
    sdk?: string;
    app_port?: number;
    subdomain?: string;
    host?: string;
    colorFrom?: string;
    colorTo?: string;
    title?: string;
    emoji?: string;
    pinned?: boolean;
    cardData?: any;
}

export interface CodeSnippet {
    language: string;
    code: string;
    description: string;
    task?: string;
}