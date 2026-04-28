/**
 * API Service - Handles REST API calls, GraphQL queries, and WebSocket connections
 */

export interface APIEndpoint {
  id: string
  name: string
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  url: string
  headers?: Record<string, string>
  params?: Record<string, any>
  body?: any
  auth?: {
    type: 'bearer' | 'basic' | 'apikey' | 'oauth'
    token?: string
    username?: string
    password?: string
    apiKey?: string
    headerName?: string
  }
  timeout?: number
  retries?: number
  responseType?: 'json' | 'text' | 'blob' | 'stream'
}

export interface GraphQLQuery {
  query: string
  variables?: Record<string, any>
  operationName?: string
}

export interface WebSocketConfig {
  url: string
  protocols?: string[]
  headers?: Record<string, string>
  reconnect?: boolean
  reconnectDelay?: number
  maxReconnectAttempts?: number
}

export interface APIResponse<T = any> {
  data: T
  status: number
  statusText: string
  headers: Record<string, string>
  config: APIEndpoint
  duration: number
}

export interface APIError {
  message: string
  status?: number
  statusText?: string
  data?: any
  config?: APIEndpoint
}

export class APIService {
  private baseURL?: string
  private defaultHeaders: Record<string, string> = {}
  private interceptors: {
    request: ((config: APIEndpoint) => APIEndpoint | Promise<APIEndpoint>)[]
    response: ((response: APIResponse) => APIResponse | Promise<APIResponse>)[]
    error: ((error: APIError) => Promise<APIError>)[]
  } = {
    request: [],
    response: [],
    error: []
  }

  constructor(baseURL?: string, defaultHeaders?: Record<string, string>) {
    this.baseURL = baseURL
    if (defaultHeaders) {
      this.defaultHeaders = { ...defaultHeaders }
    }
  }

  /**
   * Set base URL for all requests
   */
  setBaseURL(url: string): void {
    this.baseURL = url
  }

  /**
   * Set default headers for all requests
   */
  setDefaultHeaders(headers: Record<string, string>): void {
    this.defaultHeaders = { ...this.defaultHeaders, ...headers }
  }

  /**
   * Add request interceptor
   */
  addRequestInterceptor(
    interceptor: (config: APIEndpoint) => APIEndpoint | Promise<APIEndpoint>
  ): void {
    this.interceptors.request.push(interceptor)
  }

  /**
   * Add response interceptor
   */
  addResponseInterceptor(
    interceptor: (response: APIResponse) => APIResponse | Promise<APIResponse>
  ): void {
    this.interceptors.response.push(interceptor)
  }

  /**
   * Add error interceptor
   */
  addErrorInterceptor(
    interceptor: (error: APIError) => Promise<APIError>
  ): void {
    this.interceptors.error.push(interceptor)
  }

  /**
   * Make HTTP request
   */
  async request<T = any>(endpoint: APIEndpoint): Promise<APIResponse<T>> {
    const startTime = Date.now()
    
    try {
      // Apply request interceptors
      let config = { ...endpoint }
      for (const interceptor of this.interceptors.request) {
        config = await interceptor(config)
      }

      // Build final URL
      const url = this.buildURL(config.url)
      
      // Build headers
      const headers = this.buildHeaders(config.headers)
      
      // Build request options
      const options: RequestInit = {
        method: config.method,
        headers,
        signal: this.createTimeoutSignal(config.timeout)
      }

      // Add body for non-GET requests
      if (config.method !== 'GET' && config.body) {
        options.body = this.serializeBody(config.body, headers['Content-Type'])
      }

      // Add query parameters for GET requests
      const finalUrl = config.method === 'GET' && config.params
        ? this.addQueryParams(url, config.params)
        : url

      // Make request with retries
      let response: Response
      let attempt = 0
      const maxAttempts = (config.retries || 0) + 1

      while (attempt < maxAttempts) {
        try {
          response = await fetch(finalUrl, options)
          break
        } catch (error) {
          attempt++
          if (attempt >= maxAttempts) {
            throw error
          }
          await this.delay(1000 * attempt) // Exponential backoff
        }
      }

      // Parse response
      const data = await this.parseResponse<T>(response!, config.responseType)
      
      const apiResponse: APIResponse<T> = {
        data,
        status: response!.status,
        statusText: response!.statusText,
        headers: this.parseHeaders(response!.headers),
        config,
        duration: Date.now() - startTime
      }

      // Apply response interceptors
      let finalResponse = apiResponse
      for (const interceptor of this.interceptors.response) {
        finalResponse = await interceptor(finalResponse)
      }

      // Check if response is successful
      if (!response!.ok) {
        throw this.createAPIError(finalResponse)
      }

      return finalResponse

    } catch (error) {
      const apiError = error instanceof Error
        ? this.createAPIError({ message: error.message, config: endpoint })
        : error as APIError

      // Apply error interceptors
      let finalError = apiError
      for (const interceptor of this.interceptors.error) {
        finalError = await interceptor(finalError)
      }

      throw finalError
    }
  }

  /**
   * GET request
   */
  async get<T = any>(url: string, params?: Record<string, any>, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.request<T>({
      id: '',
      name: '',
      method: 'GET',
      url,
      params,
      headers
    })
  }

  /**
   * POST request
   */
  async post<T = any>(url: string, body?: any, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.request<T>({
      id: '',
      name: '',
      method: 'POST',
      url,
      body,
      headers
    })
  }

  /**
   * PUT request
   */
  async put<T = any>(url: string, body?: any, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.request<T>({
      id: '',
      name: '',
      method: 'PUT',
      url,
      body,
      headers
    })
  }

  /**
   * DELETE request
   */
  async delete<T = any>(url: string, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.request<T>({
      id: '',
      name: '',
      method: 'DELETE',
      url,
      headers
    })
  }

  /**
   * PATCH request
   */
  async patch<T = any>(url: string, body?: any, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.request<T>({
      id: '',
      name: '',
      method: 'PATCH',
      url,
      body,
      headers
    })
  }

  /**
   * GraphQL query
   */
  async graphql<T = any>(endpoint: string, query: GraphQLQuery, headers?: Record<string, string>): Promise<APIResponse<T>> {
    return this.post<T>(endpoint, query, {
      'Content-Type': 'application/json',
      ...headers
    })
  }

  /**
   * Upload file
   */
  async upload<T = any>(url: string, file: File, fieldName = 'file', additionalData?: Record<string, any>): Promise<APIResponse<T>> {
    const formData = new FormData()
    formData.append(fieldName, file)
    
    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, String(value))
      })
    }

    return this.post<T>(url, formData)
  }

  /**
   * Download file
   */
  async download(url: string, filename?: string): Promise<void> {
    const response = await this.request<Blob>({
      id: '',
      name: '',
      method: 'GET',
      url,
      responseType: 'blob'
    })

    const blob = response.data
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  }

  /**
   * Create WebSocket connection
   */
  createWebSocket(config: WebSocketConfig): WebSocket {
    const ws = new WebSocket(config.url, config.protocols)
    
    if (config.reconnect) {
      let reconnectAttempts = 0
      const maxAttempts = config.maxReconnectAttempts || 5
      const reconnectDelay = config.reconnectDelay || 5000

      ws.addEventListener('close', (event) => {
        if (reconnectAttempts < maxAttempts && !event.wasClean) {
          setTimeout(() => {
            reconnectAttempts++
            return this.createWebSocket(config)
          }, reconnectDelay * reconnectAttempts)
        }
      })

      ws.addEventListener('open', () => {
        reconnectAttempts = 0
      })
    }

    return ws
  }

  /**
   * Test API endpoint
   */
  async testEndpoint(endpoint: APIEndpoint): Promise<{ success: boolean; response?: APIResponse; error?: APIError }> {
    try {
      const response = await this.request(endpoint)
      return { success: true, response }
    } catch (error) {
      return { success: false, error: error as APIError }
    }
  }

  /**
   * Generate API documentation
   */
  generateDocumentation(endpoints: APIEndpoint[]): string {
    let doc = '# API Documentation\n\n'
    
    endpoints.forEach(endpoint => {
      doc += `## ${endpoint.name}\n\n`
      doc += `**${endpoint.method}** \`${endpoint.url}\`\n\n`
      
      if (endpoint.headers) {
        doc += '### Headers\n'
        Object.entries(endpoint.headers).forEach(([key, value]) => {
          doc += `- ${key}: ${value}\n`
        })
        doc += '\n'
      }
      
      if (endpoint.params) {
        doc += '### Parameters\n'
        Object.entries(endpoint.params).forEach(([key, value]) => {
          doc += `- ${key}: ${typeof value} = ${value}\n`
        })
        doc += '\n'
      }
      
      if (endpoint.body) {
        doc += '### Request Body\n'
        doc += '```json\n'
        doc += JSON.stringify(endpoint.body, null, 2)
        doc += '\n```\n\n'
      }
      
      doc += '---\n\n'
    })
    
    return doc
  }

  // Private helper methods
  private buildURL(url: string): string {
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    
    if (this.baseURL) {
      return `${this.baseURL.replace(/\/$/, '')}/${url.replace(/^\//, '')}`
    }
    
    return url
  }

  private buildHeaders(headers?: Record<string, string>): Record<string, string> {
    return {
      'Content-Type': 'application/json',
      ...this.defaultHeaders,
      ...headers
    }
  }

  private addQueryParams(url: string, params: Record<string, any>): string {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        searchParams.append(key, String(value))
      }
    })
    
    const queryString = searchParams.toString()
    return queryString ? `${url}?${queryString}` : url
  }

  private serializeBody(body: any, contentType?: string): string | FormData {
    if (body instanceof FormData) {
      return body
    }
    
    if (contentType?.includes('application/json')) {
      return JSON.stringify(body)
    }
    
    if (typeof body === 'string') {
      return body
    }
    
    return JSON.stringify(body)
  }

  private async parseResponse<T>(response: Response, responseType?: string): Promise<T> {
    switch (responseType) {
      case 'text':
        return response.text() as Promise<T>
      case 'blob':
        return response.blob() as Promise<T>
      case 'stream':
        return response.body as Promise<T>
      case 'json':
      default:
        const text = await response.text()
        try {
          return JSON.parse(text)
        } catch {
          return text as T
        }
    }
  }

  private parseHeaders(headers: Headers): Record<string, string> {
    const result: Record<string, string> = {}
    headers.forEach((value, key) => {
      result[key] = value
    })
    return result
  }

  private createTimeoutSignal(timeout?: number): AbortSignal | undefined {
    if (!timeout) return undefined
    
    const controller = new AbortController()
    setTimeout(() => controller.abort(), timeout)
    return controller.signal
  }

  private createAPIError(error: Partial<APIError>): APIError {
    return {
      message: error.message || 'API request failed',
      status: error.status,
      statusText: error.statusText,
      data: error.data,
      config: error.config
    }
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

/**
 * Create a singleton API service instance
 */
export const apiService = new APIService()

/**
 * API Hook for React components
 */
export function useAPI() {
  return {
    get: apiService.get.bind(apiService),
    post: apiService.post.bind(apiService),
    put: apiService.put.bind(apiService),
    delete: apiService.delete.bind(apiService),
    patch: apiService.patch.bind(apiService),
    graphql: apiService.graphql.bind(apiService),
    upload: apiService.upload.bind(apiService),
    download: apiService.download.bind(apiService),
    request: apiService.request.bind(apiService),
    testEndpoint: apiService.testEndpoint.bind(apiService)
  }
}