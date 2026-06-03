import { useState, useEffect, useCallback, useRef } from 'react'

interface CodeChange {
  filePath: string
  content: string
  timestamp: number
  changeType: 'create' | 'update' | 'delete'
}

interface PreviewState {
  isActive: boolean
  lastUpdate: number
  pendingChanges: CodeChange[]
  previewData: any
}

export function useRealTimePreview(
  projectFiles: Record<string, string>,
  onPreviewUpdate?: (previewData: any) => void
) {
  const [previewState, setPreviewState] = useState<PreviewState>({
    isActive: false,
    lastUpdate: 0,
    pendingChanges: [],
    previewData: null
  })
  
  const debounceTimer = useRef<NodeJS.Timeout>()
  const previousFiles = useRef<Record<string, string>>({})

  const processCodeChanges = useCallback((files: Record<string, string>) => {
    const changes: CodeChange[] = []
    const now = Date.now()

    // Detect changes by comparing with previous state
    Object.entries(files).forEach(([filePath, content]) => {
      const previousContent = previousFiles.current[filePath]
      
      if (previousContent === undefined) {
        // New file
        changes.push({
          filePath,
          content,
          timestamp: now,
          changeType: 'create'
        })
      } else if (previousContent !== content) {
        // Modified file
        changes.push({
          filePath,
          content,
          timestamp: now,
          changeType: 'update'
        })
      }
    })

    // Detect deleted files
    Object.keys(previousFiles.current).forEach(filePath => {
      if (!(filePath in files)) {
        changes.push({
          filePath,
          content: '',
          timestamp: now,
          changeType: 'delete'
        })
      }
    })

    previousFiles.current = { ...files }
    return changes
  }, [])

  const generatePreviewData = useCallback((files: Record<string, string>) => {
    const previewData = {
      components: [],
      activities: [],
      layouts: [],
      assets: [],
      manifest: null,
      lastGenerated: Date.now()
    }

    Object.entries(files).forEach(([filePath, content]) => {
      if (filePath.includes('layout') && filePath.endsWith('.xml')) {
        // Parse layout files
        const layoutInfo = parseLayoutFile(content, filePath)
        previewData.layouts.push(layoutInfo)
      } else if (filePath.endsWith('.kt') && content.includes('Activity')) {
        // Parse Activity files
        const activityInfo = parseActivityFile(content, filePath)
        previewData.activities.push(activityInfo)
      } else if (filePath === 'AndroidManifest.xml') {
        // Parse manifest
        previewData.manifest = parseManifestFile(content)
      } else if (filePath.includes('drawable') || filePath.includes('mipmap')) {
        // Track assets
        previewData.assets.push({
          path: filePath,
          type: getAssetType(filePath)
        })
      }
    })

    return previewData
  }, [])

  const parseLayoutFile = (content: string, filePath: string) => {
    const views = []
    const viewMatches = content.match(/<(\w+)[\s\S]*?(?:\/?>|<\/\1>)/g) || []
    
    viewMatches.forEach((match, index) => {
      const tagMatch = match.match(/<(\w+)/)
      if (tagMatch) {
        const viewType = tagMatch[1]
        const properties = {}
        
        const propMatches = match.match(/(\w+:?\w+)="([^"]+)"/g) || []
        propMatches.forEach(prop => {
          const [key, value] = prop.split('=')
          properties[key] = value.replace(/"/g, '')
        })
        
        views.push({
          id: `${viewType}_${index}`,
          type: viewType,
          properties,
          source: filePath
        })
      }
    })
    
    return {
      filePath,
      fileName: filePath.split('/').pop(),
      views,
      rootView: views[0]?.type || 'LinearLayout'
    }
  }

  const parseActivityFile = (content: string, filePath: string) => {
    const classMatch = content.match(/class\s+(\w+)/)
    const packageMatch = content.match(/package\s+([\w.]+)/)
    
    return {
      className: classMatch?.[1] || 'Unknown',
      packageName: packageMatch?.[1] || 'com.example',
      filePath,
      isMainActivity: content.includes('LAUNCHER'),
      layoutBinding: extractLayoutBinding(content)
    }
  }

  const parseManifestFile = (content: string) => {
    const packageMatch = content.match(/package="([^"]+)"/)
    const activityMatches = content.match(/<activity[\s\S]*?<\/activity>/g) || []
    
    return {
      packageName: packageMatch?.[1] || 'com.example.app',
      activities: activityMatches.length,
      hasMainActivity: content.includes('android.intent.action.MAIN')
    }
  }

  const extractLayoutBinding = (content: string) => {
    const bindingMatch = content.match(/setContentView\(R\.layout\.(\w+)\)/)
    return bindingMatch?.[1] || null
  }

  const getAssetType = (filePath: string) => {
    if (filePath.includes('drawable')) return 'drawable'
    if (filePath.includes('mipmap')) return 'icon'
    if (filePath.includes('values')) return 'resource'
    return 'unknown'
  }

  const debouncedUpdate = useCallback((files: Record<string, string>) => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current)
    }

    debounceTimer.current = setTimeout(() => {
      if (previewState.isActive) {
        const changes = processCodeChanges(files)
        const newPreviewData = generatePreviewData(files)
        
        setPreviewState(prev => ({
          ...prev,
          lastUpdate: Date.now(),
          pendingChanges: [...prev.pendingChanges, ...changes].slice(-50), // Keep last 50 changes
          previewData: newPreviewData
        }))

        onPreviewUpdate?.(newPreviewData)
      }
    }, 300) // 300ms debounce
  }, [previewState.isActive, processCodeChanges, generatePreviewData, onPreviewUpdate])

  useEffect(() => {
    if (previewState.isActive) {
      debouncedUpdate(projectFiles)
    }
  }, [projectFiles, debouncedUpdate, previewState.isActive])

  const startPreview = useCallback(() => {
    setPreviewState(prev => ({ ...prev, isActive: true }))
    // Initial parse
    const initialPreviewData = generatePreviewData(projectFiles)
    setPreviewState(prev => ({
      ...prev,
      previewData: initialPreviewData,
      lastUpdate: Date.now()
    }))
    onPreviewUpdate?.(initialPreviewData)
  }, [projectFiles, generatePreviewData, onPreviewUpdate])

  const stopPreview = useCallback(() => {
    setPreviewState(prev => ({ ...prev, isActive: false }))
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current)
    }
  }, [])

  const clearChanges = useCallback(() => {
    setPreviewState(prev => ({ ...prev, pendingChanges: [] }))
  }, [])

  return {
    isActive: previewState.isActive,
    previewData: previewState.previewData,
    pendingChanges: previewState.pendingChanges,
    lastUpdate: previewState.lastUpdate,
    startPreview,
    stopPreview,
    clearChanges
  }
}