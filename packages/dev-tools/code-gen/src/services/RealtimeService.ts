/**
 * Real-time Data Sync Service - Handles WebSocket connections and real-time updates
 */

export interface RealtimeConfig {
  url: string
  apiKey?: string
  channels?: string[]
  auth?: {
    type: 'token' | 'headers'
    token?: string
    headers?: Record<string, string>
  }
  reconnect?: boolean
  heartbeat?: number
}

export interface RealtimeMessage {
  id: string
  channel: string
  event: string
  data: any
  timestamp: number
  userId?: string
}

export interface RealtimeSubscription {
  channel: string
  callback: (message: RealtimeMessage) => void
  filter?: (message: RealtimeMessage) => boolean
}

export class RealtimeService {
  private socket: WebSocket | null = null
  private config: RealtimeConfig
  private subscriptions: Map<string, RealtimeSubscription[]> = new Map()
  private isConnected: boolean = false
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5
  private heartbeatInterval: NodeJS.Timeout | null = null

  constructor(config: RealtimeConfig) {
    this.config = config
  }

  /**
   * Connect to WebSocket server
   */
  async connect(): Promise<void> {
    try {
      this.socket = new WebSocket(this.config.url)
      
      this.socket.onopen = this.handleOpen.bind(this)
      this.socket.onmessage = this.handleMessage.bind(this)
      this.socket.onclose = this.handleClose.bind(this)
      this.socket.onerror = this.handleError.bind(this)

      return new Promise((resolve, reject) => {
        const timeout = setTimeout(() => {
          reject(new Error('Connection timeout'))
        }, 10000)

        this.socket!.onopen = () => {
          clearTimeout(timeout)
          this.handleOpen()
          resolve()
        }

        this.socket!.onerror = (error) => {
          clearTimeout(timeout)
          reject(error)
        }
      })
    } catch (error) {
      throw new Error(`Failed to connect: ${error}`)
    }
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }

    if (this.socket) {
      this.socket.close(1000, 'Client disconnect')
      this.socket = null
    }

    this.isConnected = false
    this.subscriptions.clear()
  }

  /**
   * Subscribe to a channel
   */
  subscribe(channel: string, callback: (message: RealtimeMessage) => void, filter?: (message: RealtimeMessage) => boolean): () => void {
    const subscription: RealtimeSubscription = {
      channel,
      callback,
      filter
    }

    if (!this.subscriptions.has(channel)) {
      this.subscriptions.set(channel, [])
    }

    this.subscriptions.get(channel)!.push(subscription)

    // Send subscription message to server
    if (this.isConnected) {
      this.send({
        type: 'subscribe',
        channel,
        auth: this.config.auth
      })
    }

    // Return unsubscribe function
    return () => {
      const channelSubs = this.subscriptions.get(channel)
      if (channelSubs) {
        const index = channelSubs.indexOf(subscription)
        if (index > -1) {
          channelSubs.splice(index, 1)
        }

        if (channelSubs.length === 0) {
          this.subscriptions.delete(channel)
          
          // Send unsubscribe message to server
          if (this.isConnected) {
            this.send({
              type: 'unsubscribe',
              channel
            })
          }
        }
      }
    }
  }

  /**
   * Publish message to a channel
   */
  publish(channel: string, event: string, data: any): void {
    if (!this.isConnected) {
      throw new Error('Not connected to realtime server')
    }

    const message: RealtimeMessage = {
      id: this.generateId(),
      channel,
      event,
      data,
      timestamp: Date.now()
    }

    this.send({
      type: 'publish',
      message
    })
  }

  /**
   * Join a presence channel
   */
  joinPresence(channel: string, userData: any): () => void {
    const presenceChannel = `presence-${channel}`
    
    if (this.isConnected) {
      this.send({
        type: 'presence',
        action: 'join',
        channel: presenceChannel,
        userData
      })
    }

    return () => {
      if (this.isConnected) {
        this.send({
          type: 'presence',
          action: 'leave',
          channel: presenceChannel
        })
      }
    }
  }

  /**
   * Get connection status
   */
  getStatus(): { connected: boolean; reconnectAttempts: number } {
    return {
      connected: this.isConnected,
      reconnectAttempts: this.reconnectAttempts
    }
  }

  // Private methods
  private handleOpen(): void {
    this.isConnected = true
    this.reconnectAttempts = 0

    // Setup heartbeat
    if (this.config.heartbeat) {
      this.heartbeatInterval = setInterval(() => {
        this.send({ type: 'ping' })
      }, this.config.heartbeat)
    }

    // Resubscribe to all channels
    for (const channel of this.subscriptions.keys()) {
      this.send({
        type: 'subscribe',
        channel,
        auth: this.config.auth
      })
    }
  }

  private handleMessage(event: MessageEvent): void {
    try {
      const data = JSON.parse(event.data)

      if (data.type === 'pong') {
        return // Heartbeat response
      }

      if (data.type === 'message' && data.message) {
        this.handleRealtimeMessage(data.message)
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
    }
  }

  private handleRealtimeMessage(message: RealtimeMessage): void {
    const channelSubs = this.subscriptions.get(message.channel)
    
    if (channelSubs) {
      channelSubs.forEach(sub => {
        if (!sub.filter || sub.filter(message)) {
          try {
            sub.callback(message)
          } catch (error) {
            console.error('Error in subscription callback:', error)
          }
        }
      })
    }
  }

  private handleClose(event: CloseEvent): void {
    this.isConnected = false

    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }

    // Attempt reconnection if enabled
    if (this.config.reconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
      const delay = Math.pow(2, this.reconnectAttempts) * 1000 // Exponential backoff
      this.reconnectAttempts++

      setTimeout(() => {
        this.connect().catch(error => {
          console.error('Reconnection failed:', error)
        })
      }, delay)
    }
  }

  private handleError(error: Event): void {
    console.error('WebSocket error:', error)
  }

  private send(data: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(data))
    }
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9)
  }
}

/**
 * React hook for realtime functionality
 */
import { useEffect, useRef, useState } from 'react'

export function useRealtime(config: RealtimeConfig) {
  const serviceRef = useRef<RealtimeService | null>(null)
  const [isConnected, setIsConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    serviceRef.current = new RealtimeService(config)
    
    serviceRef.current.connect()
      .then(() => {
        setIsConnected(true)
        setError(null)
      })
      .catch(err => {
        setError(err.message)
        setIsConnected(false)
      })

    return () => {
      if (serviceRef.current) {
        serviceRef.current.disconnect()
      }
    }
  }, [config.url])

  const subscribe = (channel: string, callback: (message: RealtimeMessage) => void, filter?: (message: RealtimeMessage) => boolean) => {
    if (!serviceRef.current) return () => {}
    return serviceRef.current.subscribe(channel, callback, filter)
  }

  const publish = (channel: string, event: string, data: any) => {
    if (serviceRef.current) {
      serviceRef.current.publish(channel, event, data)
    }
  }

  const joinPresence = (channel: string, userData: any) => {
    if (!serviceRef.current) return () => {}
    return serviceRef.current.joinPresence(channel, userData)
  }

  return {
    isConnected,
    error,
    subscribe,
    publish,
    joinPresence,
    service: serviceRef.current
  }
}

/**
 * Sync service for automatic data synchronization
 */
export class DataSyncService {
  private realtimeService: RealtimeService
  private localData: Map<string, any> = new Map()
  private syncCallbacks: Map<string, (data: any) => void> = new Map()

  constructor(realtimeConfig: RealtimeConfig) {
    this.realtimeService = new RealtimeService(realtimeConfig)
  }

  async connect(): Promise<void> {
    await this.realtimeService.connect()
  }

  disconnect(): void {
    this.realtimeService.disconnect()
  }

  /**
   * Sync data with real-time updates
   */
  syncData<T>(key: string, initialData: T, callback?: (data: T) => void): {
    getData: () => T
    setData: (data: T) => void
    subscribe: () => () => void
  } {
    this.localData.set(key, initialData)
    
    if (callback) {
      this.syncCallbacks.set(key, callback)
    }

    const getData = (): T => {
      return this.localData.get(key)
    }

    const setData = (data: T): void => {
      this.localData.set(key, data)
      
      // Broadcast update
      this.realtimeService.publish('data-sync', 'update', {
        key,
        data,
        timestamp: Date.now()
      })

      // Call local callback
      const cb = this.syncCallbacks.get(key)
      if (cb) {
        cb(data)
      }
    }

    const subscribe = (): (() => void) => {
      return this.realtimeService.subscribe(
        'data-sync',
        (message) => {
          if (message.event === 'update' && message.data.key === key) {
            this.localData.set(key, message.data.data)
            
            const cb = this.syncCallbacks.get(key)
            if (cb) {
              cb(message.data.data)
            }
          }
        },
        (message) => message.data.key === key
      )
    }

    return { getData, setData, subscribe }
  }

  /**
   * Create collaborative document
   */
  createCollaborativeDoc(docId: string, initialContent: string = ''): {
    getContent: () => string
    updateContent: (content: string) => void
    onContentChange: (callback: (content: string) => void) => () => void
  } {
    const docKey = `doc-${docId}`
    let content = initialContent

    const getContent = () => content

    const updateContent = (newContent: string) => {
      content = newContent
      this.realtimeService.publish(docKey, 'content-update', {
        content: newContent,
        timestamp: Date.now()
      })
    }

    const onContentChange = (callback: (content: string) => void) => {
      return this.realtimeService.subscribe(
        docKey,
        (message) => {
          if (message.event === 'content-update') {
            content = message.data.content
            callback(content)
          }
        }
      )
    }

    return { getContent, updateContent, onContentChange }
  }
}